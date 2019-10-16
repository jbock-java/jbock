package net.jbock.compiler;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;
import net.jbock.CommandLineArguments;
import net.jbock.Parameter;
import net.jbock.PositionalParameter;
import net.jbock.coerce.ParameterType;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.partitioningBy;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static javax.lang.model.element.Modifier.ABSTRACT;
import static javax.lang.model.util.ElementFilter.methodsIn;

public final class Processor extends AbstractProcessor {

  private final boolean debug;

  public Processor() {
    this(false);
  }

  // visible for testing
  Processor(boolean debug) {
    this.debug = debug;
  }

  @Override
  public Set<String> getSupportedAnnotationTypes() {
    return Stream.of(
        CommandLineArguments.class,
        Parameter.class,
        PositionalParameter.class)
        .map(Class::getCanonicalName)
        .collect(toSet());
  }

  @Override
  public SourceVersion getSupportedSourceVersion() {
    return SourceVersion.latestSupported();
  }

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment env) {
    try {
      TypeTool.init(processingEnv.getElementUtils(), processingEnv.getTypeUtils());
      processInternal(annotations, env);
    } finally {
      TypeTool.unset();
    }
    return false;
  }

  private void processInternal(Set<? extends TypeElement> annotations, RoundEnvironment env) {
    Set<String> annotationsToProcess = annotations.stream().map(TypeElement::getQualifiedName).map(Name::toString).collect(toSet());
    try {
      validateAnnotatedMethods(env, annotationsToProcess);
    } catch (ValidationException e) {
      processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, e.getMessage(), e.about);
      return;
    }
    if (!annotationsToProcess.contains(CommandLineArguments.class.getCanonicalName())) {
      return;
    }
    getSourceElements(env).forEach(this::processSourceElements);
  }

  private void processSourceElements(TypeElement sourceElement) {
    ClassName generatedClass = generatedClass(ClassName.get(sourceElement));
    try {
      validateSourceElement(sourceElement);
      List<Param> parameters = getParams(sourceElement);
      if (parameters.isEmpty()) { // javapoet #739
        throw ValidationException.create(sourceElement,
            "Define at least one abstract method");
      }

      Set<ParameterType> nonpositionalParamTypes = nonpositionalParamTypes(parameters);
      Set<ParameterType> positionalParamTypes = positionalParamTypes(parameters);
      checkOnlyOnePositionalList(parameters);
      checkRankConsistentWithPosition(parameters);

      Context context = Context.create(
          sourceElement,
          generatedClass,
          parameters,
          getOverview(sourceElement),
          nonpositionalParamTypes,
          positionalParamTypes);
      TypeSpec typeSpec = Parser.create(context).define();
      write(sourceElement, context.generatedClass(), typeSpec);
    } catch (ValidationException e) {
      processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, e.getMessage(), e.about);
    } catch (AssertionError error) {
      handleUnknownError(sourceElement, error);
    }
  }

  private static void checkOnlyOnePositionalList(List<Param> allParams) {
    allParams.stream()
        .filter(Param::isRepeatable)
        .filter(Param::isPositional)
        .skip(1).findAny().ifPresent(p -> {
      throw p.validationError("There can only be one one repeatable positional parameter.");
    });
  }

  private static void checkRankConsistentWithPosition(List<Param> allParams) {
    int currentOrdinal = -1;
    for (Param param : allParams) {
      OptionalInt order = param.positionalOrder();
      if (!order.isPresent()) {
        continue;
      }
      if (order.getAsInt() < currentOrdinal) {
        throw param.validationError("Invalid position: Optional parameters must come " +
            "after required parameters. Repeatable parameters must come last.");
      }
      currentOrdinal = order.getAsInt();
    }
  }

  private static Set<ParameterType> nonpositionalParamTypes(List<Param> parameters) {
    Set<ParameterType> paramTypes = EnumSet.noneOf(ParameterType.class);
    parameters.stream()
        .filter(p -> !p.isPositional())
        .map(p -> p.coercion().parameterType())
        .forEach(paramTypes::add);
    return paramTypes;
  }

  private static Set<ParameterType> positionalParamTypes(List<Param> parameters) {
    Set<ParameterType> paramTypes = EnumSet.noneOf(ParameterType.class);
    parameters.stream()
        .filter(Param::isPositional)
        .map(p -> p.coercion().parameterType())
        .forEach(paramTypes::add);
    return paramTypes;
  }

  private Set<TypeElement> getSourceElements(RoundEnvironment env) {
    Set<? extends Element> annotated = env.getElementsAnnotatedWith(CommandLineArguments.class);
    return ElementFilter.typesIn(annotated);
  }

  private void write(
      TypeElement sourceType,
      ClassName generatedType,
      TypeSpec definedType) {
    JavaFile.Builder builder = JavaFile.builder(generatedType.packageName(), definedType);
    JavaFile javaFile = builder.build();
    try {
      JavaFileObject sourceFile = processingEnv.getFiler()
          .createSourceFile(generatedType.toString(),
              javaFile.typeSpec.originatingElements.toArray(new Element[0]));
      try (Writer writer = sourceFile.openWriter()) {
        String sourceCode = javaFile.toString();
        writer.write(sourceCode);
        if (debug) {
          System.err.println("##############");
          System.err.println("# Debug info #");
          System.err.println("##############");
          System.err.println(sourceCode);
        }
      } catch (IOException e) {
        handleUnknownError(sourceType, e);
      }
    } catch (IOException e) {
      handleUnknownError(sourceType, e);
    }
  }

  private static final Comparator<ExecutableElement> POSITION_COMPARATOR = Comparator
      .comparingInt(e -> e.getAnnotation(PositionalParameter.class).position());

  private List<Param> getParams(TypeElement sourceElement) {
    List<ExecutableElement> abstractMethods = methodsIn(sourceElement.getEnclosedElements()).stream()
        .filter(method -> method.getModifiers().contains(ABSTRACT))
        .collect(toList());
    checkExactlyOneAnnotation(abstractMethods);
    Map<Boolean, List<ExecutableElement>> partition = abstractMethods.stream().collect(
        partitioningBy(method -> method.getAnnotation(PositionalParameter.class) != null));
    List<ExecutableElement> allNonpositional = partition.getOrDefault(false, emptyList());
    List<ExecutableElement> allPositional = partition.getOrDefault(true, emptyList());
    checkPositionUnique(allPositional);
    checkFlagsThatRequirePositional(sourceElement, allPositional);
    List<ExecutableElement> sortedPositional = allPositional.stream().sorted(POSITION_COMPARATOR).collect(Collectors.toList());
    List<Param> result = new ArrayList<>(abstractMethods.size());
    for (int i = 0; i < sortedPositional.size(); i++) {
      ExecutableElement method = sortedPositional.get(i);
      Param param = Param.create(result, method, i, getDescription(method));
      result.add(param);
    }
    for (ExecutableElement method : allNonpositional) {
      Param param = Param.create(result, method, null, getDescription(method));
      result.add(param);
    }
    if (sourceElement.getAnnotation(CommandLineArguments.class).allowHelpOption()) {
      checkHelp(result);
    }
    return result;
  }

  private void checkPositionUnique(List<ExecutableElement> allPositional) {
    Set<Integer> positions = new HashSet<>();
    for (ExecutableElement method : allPositional) {
      Integer position = method.getAnnotation(PositionalParameter.class).position();
      if (!positions.add(position)) {
        throw ValidationException.create(method, "Define a unique position.");
      }
    }
  }

  private void checkFlagsThatRequirePositional(TypeElement sourceType, List<ExecutableElement> allPositional) {
    CommandLineArguments annotation = sourceType.getAnnotation(CommandLineArguments.class);
    if (annotation.allowEscapeSequence()) {
      if (allPositional.isEmpty()) {
        throw ValidationException.create(sourceType,
            "Define a positional parameter, or disable the escape sequence.");
      }
    }
  }

  private void checkExactlyOneAnnotation(List<ExecutableElement> abstractMethods) {
    for (ExecutableElement method : abstractMethods) {
      boolean isPositional = method.getAnnotation(PositionalParameter.class) != null;
      if (!isPositional && method.getAnnotation(Parameter.class) == null) {
        throw ValidationException.create(method,
            String.format("Add %s or %s annotation",
                Parameter.class.getSimpleName(), PositionalParameter.class.getSimpleName()));
      }
      if (isPositional && method.getAnnotation(Parameter.class) != null) {
        throw ValidationException.create(method,
            String.format("Remove %s or %s annotation",
                Parameter.class.getSimpleName(), PositionalParameter.class.getSimpleName()));
      }
    }
  }

  private void validateSourceElement(TypeElement sourceElement) {
    if (sourceElement.getKind() == ElementKind.INTERFACE) {
      throw ValidationException.create(sourceElement,
          "Use an abstract class, not an interface.");
    }
    if (!TypeTool.get().isSameType(sourceElement.getSuperclass(), Object.class)) {
      throw ValidationException.create(sourceElement,
          String.format("The class may not extend %s.", sourceElement.getSuperclass()));
    }
    if (!sourceElement.getModifiers().contains(ABSTRACT)) {
      throw ValidationException.create(sourceElement,
          "Use an abstract class.");
    }
    if (sourceElement.getModifiers().contains(Modifier.PRIVATE)) {
      throw ValidationException.create(sourceElement,
          "The class cannot not be private.");
    }
    if (sourceElement.getNestingKind().isNested() &&
        !sourceElement.getModifiers().contains(Modifier.STATIC)) {
      throw ValidationException.create(sourceElement,
          "The nested class must be static.");
    }
    if (!sourceElement.getInterfaces().isEmpty()) {
      throw ValidationException.create(sourceElement,
          "The class cannot implement anything.");
    }
    if (!sourceElement.getTypeParameters().isEmpty()) {
      throw ValidationException.create(sourceElement,
          "The class cannot have type parameters.");
    }
    if (!Util.hasDefaultConstructor(sourceElement)) {
      throw ValidationException.create(sourceElement,
          "The class must have a default constructor.");
    }
  }

  private List<String> getOverview(TypeElement sourceType) {
    String docComment = processingEnv.getElementUtils().getDocComment(sourceType);
    if (docComment == null) {
      return emptyList();
    }
    return Arrays.asList(tokenize(docComment));
  }

  private String[] getDescription(ExecutableElement method) {
    String docComment = processingEnv.getElementUtils().getDocComment(method);
    if (docComment == null) {
      return new String[0];
    }
    return tokenize(docComment);
  }

  private static String[] tokenize(String docComment) {
    String[] tokens = docComment.trim().split("\\n", -1);
    for (int i = 0; i < tokens.length; i++) {
      tokens[i] = tokens[i].trim();
    }
    return tokens;
  }

  private void checkHelp(List<Param> parameters) {
    for (Param param : parameters) {
      if ("help".equals(param.longName())) {
        throw param.validationError("'help' is reserved. " +
            "Either disable the help feature " +
            "or change the long name to something else.");
      }
    }
  }

  private void validateAnnotatedMethods(
      RoundEnvironment env, Set<String> annotationsToProcess) {
    List<ExecutableElement> methods = getAnnotatedMethods(env, annotationsToProcess);
    for (ExecutableElement method : methods) {
      Element enclosingElement = method.getEnclosingElement();
      if (enclosingElement.getAnnotation(CommandLineArguments.class) == null) {
        throw ValidationException.create(enclosingElement,
            "The class must have the " +
                CommandLineArguments.class.getSimpleName() + " annotation.");
      }
      if (!enclosingElement.getModifiers().contains(ABSTRACT)) {
        throw ValidationException.create(enclosingElement,
            "The class must be abstract");
      }
      if (!method.getModifiers().contains(ABSTRACT)) {
        throw ValidationException.create(method,
            "The method must be abstract.");
      }
      if (!method.getParameters().isEmpty()) {
        throw ValidationException.create(method,
            "The method may not have parameters.");
      }
      if (!method.getTypeParameters().isEmpty()) {
        throw ValidationException.create(method,
            "The method may not have type parameters.");
      }
      if (!method.getThrownTypes().isEmpty()) {
        throw ValidationException.create(method,
            "The method may not declare any exceptions.");
      }
    }
  }

  private List<ExecutableElement> getAnnotatedMethods(
      RoundEnvironment env, Set<String> annotationsToProcess) {
    Set<? extends Element> parameters =
        annotationsToProcess.contains(Parameter.class.getCanonicalName()) ?
            env.getElementsAnnotatedWith(Parameter.class) :
            emptySet();
    Set<? extends Element> positionalParams =
        annotationsToProcess.contains(PositionalParameter.class.getCanonicalName()) ?
            env.getElementsAnnotatedWith(PositionalParameter.class) :
            emptySet();
    List<ExecutableElement> methods = new ArrayList<>(parameters.size() + positionalParams.size());
    methods.addAll(methodsIn(parameters));
    methods.addAll(methodsIn(positionalParams));
    return methods;
  }

  private static ClassName generatedClass(ClassName type) {
    String name = String.join("_", type.simpleNames()) + "_Parser";
    return type.topLevelClassName().peerClass(name);
  }

  private void handleUnknownError(
      TypeElement sourceType,
      Throwable e) {
    String message = String.format("JBOCK: Unexpected error while processing %s: %s", sourceType, e.getMessage());
    e.printStackTrace(System.err);
    processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, message, sourceType);
  }
}
