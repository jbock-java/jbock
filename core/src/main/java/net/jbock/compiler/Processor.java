package net.jbock.compiler;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;
import net.jbock.CommandLineArguments;
import net.jbock.Parameter;
import net.jbock.PositionalParameter;
import net.jbock.coerce.mappers.StandardCoercions;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.OptionalInt;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.partitioningBy;
import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static javax.lang.model.element.Modifier.ABSTRACT;
import static javax.lang.model.util.ElementFilter.methodsIn;

public final class Processor extends AbstractProcessor {

  private final Set<String> done = new HashSet<>();

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
      checkAllAnnotatedMethodsValid(env);
    } catch (ValidationException e) {
      processingEnv.getMessager().printMessage(e.kind, e.getMessage(), e.about);
      return false;
    }
    for (TypeElement sourceType : getAnnotatedClasses(env)) {
      try {
        TypeTool.setInstance(processingEnv.getTypeUtils(), processingEnv.getElementUtils());
        validateType(sourceType);
        List<Param> parameters = getParams(sourceType);
        if (parameters.isEmpty()) {
          processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING,
              "Define at least one abstract method", sourceType);
        }

        Set<OptionType> paramTypes = nonpositionalParamTypes(parameters);
        Set<OptionType> positionalParamTypes = positionalParamTypes(parameters);
        Context context = Context.create(
            getOverview(sourceType),
            sourceType,
            parameters,
            paramTypes,
            positionalParamTypes);
        if (!done.add(sourceType.getQualifiedName().toString())) {
          continue;
        }
        TypeSpec typeSpec = Parser.create(context).define();
        write(context.generatedClass, typeSpec);
      } catch (ValidationException e) {
        processingEnv.getMessager().printMessage(e.kind, e.getMessage(), e.about);
      } catch (Exception e) {
        handleException(sourceType, e);
      } finally {
        TypeTool.unset();
        StandardCoercions.unset();
      }
    }
    return false;
  }

  private static Set<OptionType> nonpositionalParamTypes(List<Param> parameters) {
    Set<OptionType> paramTypes = EnumSet.noneOf(OptionType.class);
    parameters.stream()
        .filter(p -> !p.isPositional())
        .map(p -> p.paramType)
        .forEach(paramTypes::add);
    return paramTypes;
  }

  private static Set<OptionType> positionalParamTypes(List<Param> parameters) {
    Set<OptionType> paramTypes = EnumSet.noneOf(OptionType.class);
    parameters.stream()
        .filter(Param::isPositional)
        .map(p -> p.paramType)
        .forEach(paramTypes::add);
    return paramTypes;
  }

  private Set<TypeElement> getAnnotatedClasses(RoundEnvironment env) {
    Set<? extends Element> annotated = env.getElementsAnnotatedWith(CommandLineArguments.class);
    return ElementFilter.typesIn(annotated);
  }

  private void handleException(
      TypeElement sourceType,
      Exception e) {
    String message = "Unexpected error while processing " +
        ClassName.get(sourceType) +
        ": " + e.getMessage();
    e.printStackTrace();
    printError(sourceType, message);
  }

  private void write(
      ClassName generatedType,
      TypeSpec typeSpec) throws IOException {
    JavaFile.Builder builder = JavaFile.builder(generatedType.packageName(), typeSpec);
    JavaFile javaFile = builder
        .skipJavaLangImports(true)
        .build();
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
    }
  }

  private static Comparator<ExecutableElement> POSITION_COMPARATOR = Comparator
      .comparingInt(e -> e.getAnnotation(PositionalParameter.class).position());

  private PositionalOrder getPositionalOrder(ExecutableElement sourceMethod) {
    PositionalParameter parameter = sourceMethod.getAnnotation(PositionalParameter.class);
    if (parameter.repeatable()) {
      return PositionalOrder.LIST;
    }
    return parameter.optional() ? PositionalOrder.OPTIONAL : PositionalOrder.REQUIRED;
  }

  private List<Param> getParams(TypeElement sourceType) {
    checkNoSuperclass(sourceType);
    List<ExecutableElement> abstractMethods = methodsIn(sourceType.getEnclosedElements()).stream()
        .filter(method -> method.getModifiers().contains(ABSTRACT))
        .collect(toList());
    checkExactlyOneAnnotation(abstractMethods);
    Map<Boolean, List<ExecutableElement>> partition = abstractMethods.stream().collect(
        partitioningBy(method -> method.getAnnotation(PositionalParameter.class) != null));
    List<ExecutableElement> allNonpositional = partition.getOrDefault(false, emptyList());
    List<ExecutableElement> allPositional = partition.getOrDefault(true, emptyList());
    Map<PositionalOrder, List<ExecutableElement>> positionalGroups = allPositional.stream()
        .collect(groupingBy(
            this::getPositionalOrder,
            HashMap::new,
            toCollection(ArrayList::new)));
    for (List<ExecutableElement> value : positionalGroups.values()) {
      value.sort(POSITION_COMPARATOR);
    }

    checkPositionalRepeatable(sourceType, positionalGroups);
    checkPositionalOrderUnique(positionalGroups);
    List<ExecutableElement> sortedPositional = getSortedPositional(positionalGroups);
    List<Param> result = new ArrayList<>(abstractMethods.size());
    for (int i = 0; i < sortedPositional.size(); i++) {
      ExecutableElement method = sortedPositional.get(i);
      Param param = Param.create(result, method, OptionalInt.of(i), getDescription(method));
      result.add(param);
    }
    for (ExecutableElement method : allNonpositional) {
      Param param = Param.create(result, method, OptionalInt.empty(), getDescription(method));
      result.add(param);
    }
    if (sourceType.getAnnotation(CommandLineArguments.class).allowHelpOption()) {
      checkHelp(result);
    }
    return result;
  }

  private List<ExecutableElement> getSortedPositional(Map<PositionalOrder, List<ExecutableElement>> positionalGroups) {
    List<ExecutableElement> sortedPositional = new ArrayList<>();
    for (PositionalOrder positionalOrder : PositionalOrder.values()) {
      sortedPositional.addAll(positionalGroups.getOrDefault(positionalOrder, emptyList()));
    }
    return sortedPositional;
  }

  private void checkPositionalOrderUnique(Map<PositionalOrder, List<ExecutableElement>> positionalGroups) {
    for (PositionalOrder positionalOrder : PositionalOrder.values()) {
      Integer previousPosition = null;
      List<ExecutableElement> positional = positionalGroups.getOrDefault(positionalOrder, emptyList());
      for (ExecutableElement method : positional) {
        Integer position = method.getAnnotation(PositionalParameter.class).position();
        if (Objects.equals(position, previousPosition)) {
          throw ValidationException.create(method, "Define a unique position.");
        }
        previousPosition = position;
      }
    }
  }

  private void checkPositionalRepeatable(TypeElement sourceType, Map<PositionalOrder, List<ExecutableElement>> positionalGroups) {
    List<ExecutableElement> positionalRepeatable = positionalGroups.getOrDefault(PositionalOrder.LIST, emptyList());
    if (positionalRepeatable.size() >= 2) {
      throw ValidationException.create(positionalRepeatable.get(1),
          "There can only be one one repeatable positional parameter.");
    }
    CommandLineArguments annotation = sourceType.getAnnotation(CommandLineArguments.class);
    if (annotation.allowEscapeSequence()) {
      if (positionalGroups.values().stream().allMatch(List::isEmpty)) {
        throw ValidationException.create(sourceType,
            "Define a positional parameter, or disable the escape sequence.");
      }
    }
    if (annotation.allowPrefixedTokens()) {
      if (positionalGroups.values().stream().allMatch(List::isEmpty)) {
        throw ValidationException.create(sourceType,
            "Define a positional parameter, or disallow prefixed tokens.");
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

  private void validateType(TypeElement sourceType) {
    if (sourceType.getKind() == ElementKind.INTERFACE) {
      throw ValidationException.create(sourceType,
          "Use an abstract class, not an interface.");
    }
    if (!sourceType.getModifiers().contains(ABSTRACT)) {
      throw ValidationException.create(sourceType,
          "Use an abstract class.");
    }
    if (sourceType.getModifiers().contains(Modifier.PRIVATE)) {
      throw ValidationException.create(sourceType,
          "The class cannot not be private.");
    }
    if (sourceType.getNestingKind().isNested() &&
        !sourceType.getModifiers().contains(Modifier.STATIC)) {
      throw ValidationException.create(sourceType,
          "The nested class must be static.");
    }
    if (!sourceType.getInterfaces().isEmpty()) {
      throw ValidationException.create(sourceType,
          "The class cannot implement anything.");
    }
    if (!sourceType.getTypeParameters().isEmpty()) {
      throw ValidationException.create(sourceType,
          "The class cannot have type parameters.");
    }
    if (!Util.checkDefaultConstructorExists(sourceType)) {
      throw ValidationException.create(sourceType,
          "The class must have a default constructor.");
    }
  }

  private void checkNoSuperclass(TypeElement sourceType) {
    TypeMirror objectType = processingEnv.getElementUtils().getTypeElement(Object.class.getCanonicalName()).asType();
    if (!processingEnv.getTypeUtils().isSameType(sourceType.getSuperclass(), objectType)) {
      throw ValidationException.create(sourceType,
          sourceType.getSimpleName() + " may not extend anything");
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
        throw ValidationException.create(param.sourceMethod,
            "'help' is reserved. " +
                "Either disable the help feature " +
                "or change the long name to something else.");
      }
    }
  }

  private void checkAllAnnotatedMethodsValid(RoundEnvironment env) {
    Set<? extends Element> rawParams =
        env.getElementsAnnotatedWith(Parameter.class);
    Set<? extends Element> rawPositionalParams =
        env.getElementsAnnotatedWith(PositionalParameter.class);
    List<ExecutableElement> methods = new ArrayList<>(rawParams.size() + rawPositionalParams.size());
    methods.addAll(methodsIn(rawParams));
    methods.addAll(methodsIn(rawPositionalParams));
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

  private void printError(Element element, String message) {
    processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, message, element);
  }
}
