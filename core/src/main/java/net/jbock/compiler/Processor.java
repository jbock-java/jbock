package net.jbock.compiler;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;
import net.jbock.Command;
import net.jbock.Option;
import net.jbock.Param;
import net.jbock.coerce.SuppliedClassValidator;
import net.jbock.compiler.view.GeneratedClass;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.OptionalInt;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    return Stream.of(Command.class, Option.class, Param.class)
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
      getAnnotatedMethods(env, annotations).forEach(method -> {
        checkEnclosingElementIsAnnotated(method);
        validateParameterMethods(method);
      });
    } catch (ValidationException e) {
      processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, e.getMessage(), e.about);
      return false;
    }
    if (annotations.stream().map(TypeElement::getQualifiedName)
        .noneMatch(name -> name.contentEquals(Command.class.getCanonicalName()))) {
      return false;
    }
    getAnnotatedTypes(env).forEach(this::processSourceElements);
    return false;
  }

  private void processSourceElements(TypeElement sourceElement) {
    TypeTool tool = new TypeTool(processingEnv.getElementUtils(), processingEnv.getTypeUtils());
    ClassName generatedClass = generatedClass(sourceElement);
    try {
      validateSourceElement(tool, sourceElement);
      List<Parameter> parameters = getParams(tool, sourceElement);
      if (parameters.isEmpty()) { // javapoet #739
        throw ValidationException.create(sourceElement, "Define at least one abstract method");
      }

      checkOnlyOnePositionalList(parameters);
      checkRankConsistentWithPosition(parameters);

      Context context = Context.create(
          sourceElement,
          generatedClass,
          parameters,
          positionalParameters(parameters));
      TypeSpec typeSpec = GeneratedClass.create(context).define();
      write(sourceElement, context.generatedClass(), typeSpec);
    } catch (ValidationException e) {
      processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, e.getMessage(), e.about);
    } catch (AssertionError error) {
      handleUnknownError(sourceElement, error);
    }
  }

  private static void checkOnlyOnePositionalList(List<Parameter> allParams) {
    allParams.stream()
        .filter(Parameter::isRepeatable)
        .filter(Parameter::isPositional)
        .skip(1).findAny().ifPresent(p -> {
      throw p.validationError("There can only be one one repeatable positional parameter.");
    });
  }

  private static void checkRankConsistentWithPosition(List<Parameter> allParams) {
    int currentOrdinal = -1;
    for (Parameter param : allParams) {
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

  private static List<Parameter> positionalParameters(List<Parameter> parameters) {
    return parameters.stream().filter(Parameter::isPositional).collect(Collectors.toList());
  }

  private Set<TypeElement> getAnnotatedTypes(RoundEnvironment env) {
    Set<? extends Element> annotated = env.getElementsAnnotatedWith(Command.class);
    return ElementFilter.typesIn(annotated);
  }

  private void write(
      TypeElement sourceElement,
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
        handleUnknownError(sourceElement, e);
      }
    } catch (IOException e) {
      handleUnknownError(sourceElement, e);
    }
  }

  private List<Parameter> getParams(TypeTool tool, TypeElement sourceElement) {
    List<ExecutableElement> abstractMethods = methodsIn(sourceElement.getEnclosedElements()).stream()
        .filter(method -> method.getModifiers().contains(ABSTRACT))
        .collect(Collectors.toList());
    abstractMethods.forEach(Processor::validateParameterMethods);
    ParameterMethods methods = ParameterMethods.create(abstractMethods);
    boolean anyMnemonics = methods.anyMnemonics();
    List<Parameter> result = new ArrayList<>(methods.options().size() + methods.positionals().size());
    for (int i = 0; i < methods.positionals().size(); i++) {
      ExecutableElement method = methods.positionals().get(i);
      Parameter param = Parameter.create(anyMnemonics, tool, result, method, i, getDescription(method));
      result.add(param);
    }
    for (ExecutableElement method : methods.options()) {
      Parameter param = Parameter.create(anyMnemonics, tool, result, method, null, getDescription(method));
      result.add(param);
    }
    if (!sourceElement.getAnnotation(Command.class).helpDisabled()) {
      checkHelp(result);
    }
    return result;
  }

  private void validateSourceElement(TypeTool tool, TypeElement sourceElement) {
    SuppliedClassValidator.commonChecks(sourceElement);
    if (!tool.isSameType(sourceElement.getSuperclass(), Object.class) ||
        !sourceElement.getInterfaces().isEmpty()) {
      throw ValidationException.create(sourceElement, "The model class may not implement or extend anything.");
    }
    if (!sourceElement.getTypeParameters().isEmpty()) {
      throw ValidationException.create(sourceElement, "The class cannot have type parameters.");
    }
  }

  private String[] getDescription(ExecutableElement method) {
    String docComment = processingEnv.getElementUtils().getDocComment(method);
    if (docComment == null) {
      return new String[0];
    }
    return tokenizeJavadoc(docComment);
  }

  private static String[] tokenizeJavadoc(String docComment) {
    String[] tokens = docComment.trim().split("\\R", -1);
    List<String> result = new ArrayList<>(tokens.length);
    for (String t : tokens) {
      String token = t.trim();
      if (token.startsWith("@")) {
        return result.toArray(new String[0]);
      }
      if (!token.isEmpty()) {
        result.add(token);
      }
    }
    return result.toArray(new String[0]);
  }

  private void checkHelp(List<Parameter> parameters) {
    for (Parameter param : parameters) {
      param.longName().ifPresent(longName -> {
        if ("help".equals(longName)) {
          throw param.validationError("'help' is reserved. " +
              "Either disable the help feature " +
              "or change the long name to something else.");
        }
      });
    }
  }

  private static void validateParameterMethods(ExecutableElement method) {
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
    if (method.getAnnotation(Param.class) == null && method.getAnnotation(Option.class) == null) {
      throw ValidationException.create(method,
          String.format("Annotate this method with either @%s or @%s",
              Option.class.getSimpleName(), Param.class.getSimpleName()));
    }
    if (method.getAnnotation(Param.class) != null && method.getAnnotation(Option.class) != null) {
      throw ValidationException.create(method,
          String.format("Use either @%s or @%s annotation, but not both",
              Option.class.getSimpleName(), Param.class.getSimpleName()));
    }
  }

  private List<ExecutableElement> getAnnotatedMethods(RoundEnvironment env, Set<? extends TypeElement> annotations) {
    List<ExecutableElement> methods = new ArrayList<>();
    for (Class<? extends Annotation> annotation : Arrays.asList(Option.class, Param.class)) {
      if (annotations.stream().map(TypeElement::getQualifiedName)
          .anyMatch(name -> name.contentEquals(annotation.getCanonicalName()))) {
        methods.addAll(methodsIn(env.getElementsAnnotatedWith(annotation)));
      }
    }
    return methods;
  }

  private static ClassName generatedClass(TypeElement sourceElement) {
    ClassName type = ClassName.get(sourceElement);
    String name = String.join("_", type.simpleNames()) + "_Parser";
    return type.topLevelClassName().peerClass(name);
  }

  private void handleUnknownError(TypeElement sourceType, Throwable e) {
    String message = String.format("JBOCK: Unexpected error while processing %s: %s", sourceType, e.getMessage());
    e.printStackTrace(System.err);
    processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, message, sourceType);
  }

  private void checkEnclosingElementIsAnnotated(ExecutableElement method) {
    Element enclosingElement = method.getEnclosingElement();
    if (enclosingElement.getKind() != ElementKind.CLASS) {
      throw ValidationException.create(enclosingElement, "The enclosing element must be a class.");
    }
    if (enclosingElement.getAnnotation(Command.class) == null) {
      throw ValidationException.create(enclosingElement,
          "The class must have the @" + Command.class.getSimpleName() + " annotation.");
    }
  }
}
