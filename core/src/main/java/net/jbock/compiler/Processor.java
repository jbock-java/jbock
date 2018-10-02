package net.jbock.compiler;

import net.jbock.CommandLineArguments;
import net.jbock.Parameter;
import net.jbock.PositionalParameter;
import net.jbock.coerce.mappers.AllCoercions;
import net.jbock.com.squareup.javapoet.ClassName;
import net.jbock.com.squareup.javapoet.JavaFile;
import net.jbock.com.squareup.javapoet.TypeSpec;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.util.ElementFilter;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static javax.lang.model.element.Modifier.ABSTRACT;
import static javax.lang.model.util.ElementFilter.methodsIn;
import static net.jbock.compiler.Util.asDeclared;
import static net.jbock.compiler.Util.asType;

public final class Processor extends AbstractProcessor {

  private final Set<String> done = new HashSet<>();

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
        List<Param> parameters = validate(sourceType);
        if (parameters.isEmpty()) {
          processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING,
              "Skipping code generation: No abstract methods found", sourceType);
          continue;
        }

        Set<OptionType> paramTypes = nonpositionalParamTypes(parameters);
        Set<OptionType> positionalParamTypes = positionalParamTypes(parameters);
        Context context = Context.create(
            getOverview(sourceType),
            sourceType,
            parameters,
            paramTypes,
            positionalParamTypes);
        if (!done.add(asType(sourceType).getQualifiedName().toString())) {
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
        AllCoercions.unset();
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
        ClassName.get(asType(sourceType)) +
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
      writer.write(javaFile.toString());
    }
  }

  private List<Param> validate(TypeElement sourceType) {
    if (sourceType.getKind() == ElementKind.INTERFACE) {
      throw ValidationException.create(sourceType,
          sourceType.getSimpleName() + " must be an abstract class, not an interface");
    }
    if (!sourceType.getModifiers().contains(ABSTRACT)) {
      throw ValidationException.create(sourceType,
          sourceType.getSimpleName() + " must be abstract");
    }
    if (sourceType.getModifiers().contains(Modifier.PRIVATE)) {
      throw ValidationException.create(sourceType,
          sourceType.getSimpleName() + " may not be private");
    }
    if (sourceType.getNestingKind().isNested() &&
        !sourceType.getModifiers().contains(Modifier.STATIC)) {
      throw ValidationException.create(sourceType,
          "The nested class " + sourceType.getSimpleName() + " must be static");
    }
    if (!sourceType.getInterfaces().isEmpty()) {
      throw ValidationException.create(sourceType,
          sourceType.getSimpleName() + " may not implement " +
              asDeclared(sourceType.getInterfaces().get(0)).asElement().getSimpleName());
    }
    if (sourceType.getSuperclass().getKind() == TypeKind.DECLARED &&
        !Util.equalsType(sourceType.getSuperclass(), "java.lang.Object")) {
      throw ValidationException.create(sourceType,
          sourceType.getSimpleName() + " may not extend " +
              asDeclared(sourceType.getSuperclass()).asElement().getSimpleName());
    }
    List<ExecutableElement> abstractMethods = methodsIn(sourceType.getEnclosedElements()).stream()
        .filter(method -> method.getModifiers().contains(ABSTRACT))
        .collect(toList());
    List<Param> parameters = new ArrayList<>(abstractMethods.size());
    int positionalIndex = -1;
    for (ExecutableElement method : abstractMethods) {
      boolean isPositional = method.getAnnotation(PositionalParameter.class) != null;
      if (!isPositional && method.getAnnotation(Parameter.class) == null) {
        throw ValidationException.create(method, "Expecting either Parameter or PositionalParameter annotation");
      }
      if (isPositional) {
        positionalIndex++;
      }
      Param param = Param.create(parameters, method, positionalIndex, getDescription(method));
      parameters.add(param);
    }
    if (sourceType.getAnnotation(CommandLineArguments.class).addHelp()) {
      checkHelp(parameters);
    }
    checkOnlyOnePositionalList(parameters);
    checkPositionalOrder(parameters);
    return parameters;
  }

  private List<String> getOverview(TypeElement sourceType) {
    String docComment = processingEnv.getElementUtils().getDocComment(sourceType);
    if (docComment == null) {
      return Collections.emptyList();
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

  private void checkOnlyOnePositionalList(List<Param> params) {
    boolean positionalListFound = false;
    for (Param param : params) {
      if (param.isPositional() &&
          param.paramType == OptionType.REPEATABLE) {
        if (positionalListFound) {
          throw ValidationException.create(param.sourceMethod,
              "Only one positional list allowed");
        }
        positionalListFound = true;
      }
    }
  }

  private void checkPositionalOrder(List<Param> params) {
    Param previousPositional = null;
    for (Param param : params) {
      if (!param.isPositional()) {
        continue;
      }
      if (previousPositional != null) {
        PositionalOrder paramOrder = param.positionalOrder();
        PositionalOrder previousOrder = previousPositional.positionalOrder();
        if (paramOrder == null || previousOrder == null) {
          throw new AssertionError();
        }
        if (paramOrder.compareTo(previousOrder) < 0) {
          throw ValidationException.create(param.sourceMethod,
              String.format("Positional order: %s method %s() must come before %s method %s()",
                  paramOrder, param.methodName(),
                  previousOrder, previousPositional.methodName()));
        }
      }
      previousPositional = param;
    }
  }

  static void checkNotPresent(
      ExecutableElement executableElement,
      Annotation cause,
      List<Class<? extends Annotation>> forbiddenAnnotations) {
    for (Class<? extends Annotation> annotation : forbiddenAnnotations) {
      if (executableElement.getAnnotation(annotation) != null) {
        throw ValidationException.create(executableElement,
            String.format("Cannot have both of %s and %s",
                annotation.getSimpleName(), cause.annotationType().getSimpleName()));
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
