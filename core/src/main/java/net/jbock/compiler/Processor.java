package net.jbock.compiler;

import net.jbock.CommandLineArguments;
import net.jbock.Description;
import net.jbock.LongName;
import net.jbock.Positional;
import net.jbock.ShortName;
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
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static javax.lang.model.element.ElementKind.METHOD;
import static javax.lang.model.element.Modifier.ABSTRACT;
import static javax.lang.model.util.ElementFilter.methodsIn;
import static net.jbock.compiler.Util.asDeclared;
import static net.jbock.compiler.Util.asType;
import static net.jbock.compiler.Util.methodToString;

public final class Processor extends AbstractProcessor {

  private final Set<String> done = new HashSet<>();

  @Override
  public Set<String> getSupportedAnnotationTypes() {
    return Stream.of(
        CommandLineArguments.class,
        Description.class,
        LongName.class,
        Positional.class,
        ShortName.class)
        .map(Class::getName)
        .collect(toSet());
  }

  @Override
  public SourceVersion getSupportedSourceVersion() {
    return SourceVersion.latestSupported();
  }

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment env) {
    if (!checkValid(env)) {
      return false;
    }
    for (TypeElement sourceType : getAnnotatedClasses(env)) {
      try {
        List<Param> parameters = validate(sourceType);
        if (parameters.isEmpty()) {
          processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING,
              "Skipping code generation: No abstract methods found", sourceType);
          continue;
        }
        Set<Type> paramTypes = nonpositionalParamTypes(parameters);
        Set<PositionalType> positionalParamTypes = positionalParamTypes(parameters);
        Context context = Context.create(
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
      }
    }
    return false;
  }

  private static Set<Type> nonpositionalParamTypes(List<Param> parameters) {
    Set<Type> paramTypes = EnumSet.noneOf(Type.class);
    parameters.stream()
        .filter(p -> !p.isPositional())
        .map(p -> p.paramType)
        .forEach(paramTypes::add);
    return paramTypes;
  }

  private static Set<PositionalType> positionalParamTypes(List<Param> parameters) {
    Set<PositionalType> paramTypes = EnumSet.noneOf(PositionalType.class);
    parameters.stream()
        .filter(Param::isPositional)
        .map(Param::positionalType)
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

  private List<Param> checkPositionalOrder(List<Param> params) {
    List<Param> result = new ArrayList<>(params.size());
    Param previousPositional = null;
    for (Param param : params) {
      if (!param.isPositional()) {
        result.add(param);
        continue;
      }
      validatePositionalOrder(previousPositional, param);
      result.add(param);
      previousPositional = param;
    }
    return result;
  }

  private void validatePositionalOrder(Param previous, Param param) {
    if (previous == null) {
      return;
    }
    if (param.positionalType().order.compareTo(previous.positionalType().order) < 0) {
      throw ValidationException.create(param.sourceMethod,
          String.format("Positional order: %s method %s() must come before %s method %s()",
              param.positionalType().order, param.methodName(),
              previous.positionalType().order, previous.methodName()));
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
    List<ExecutableElement> abstractMethods = sourceType.getEnclosedElements().stream()
        .filter(element -> element.getKind() == METHOD)
        .map(method -> (ExecutableElement) method)
        .filter(method -> method.getModifiers().contains(ABSTRACT))
        .collect(toList());
    List<Param> parameters = new ArrayList<>(abstractMethods.size());
    for (int index = 0; index < abstractMethods.size(); index++) {
      ExecutableElement method = abstractMethods.get(index);
      Param param = Param.create(parameters, method, index);
      if (Objects.equals("help", param.longName()) &&
          sourceType.getAnnotation(CommandLineArguments.class).addHelp()) {
        throw ValidationException.create(method, "'--help' is a special token, see CommandLineArguments#addHelp");
      }
      parameters.add(param);
    }
    checkOnlyOnePositionalList(parameters);
    checkDistinctLongNames(parameters);
    checkDistinctShortNames(parameters);
    return checkPositionalOrder(parameters);
  }

  private void checkDistinctShortNames(List<Param> params) {
    Set<Character> names = new HashSet<>(params.size());
    for (Param param : params) {
      Character name = param.shortName();
      if (name != null) {
        boolean added = names.add(name);
        if (!added) {
          throw ValidationException.create(param.sourceMethod,
              "Duplicate short name: " + name);
        }
      }
    }
  }

  private void checkOnlyOnePositionalList(List<Param> params) {
    boolean positionalListFound = false;
    for (Param param : params) {
      if (param.isPositional() &&
          param.paramType == Type.REPEATABLE) {
        if (positionalListFound) {
          throw ValidationException.create(param.sourceMethod,
              "Only one positional list allowed");
        }
        positionalListFound = true;
      }
    }
  }

  private void checkDistinctLongNames(List<Param> params) {
    Set<String> names = new HashSet<>(params.size());
    for (Param param : params) {
      String name = param.longName();
      if (name != null) {
        boolean added = names.add(name);
        if (!added) {
          throw ValidationException.create(param.sourceMethod,
              "Duplicate long name: " + name);
        }
      }
    }
  }

  static void checkNotPresent(
      ExecutableElement executableElement,
      Annotation cause,
      List<Class<? extends Annotation>> forbiddenAnnotations) {
    for (Class<? extends Annotation> annotation : forbiddenAnnotations) {
      if (executableElement.getAnnotation(annotation) != null) {
        throw ValidationException.create(executableElement,
            "@" + annotation.getSimpleName() +
                " is conflicting with @" + cause.annotationType().getSimpleName());
      }
    }
  }

  private boolean checkValid(RoundEnvironment env) {
    List<ExecutableElement> methods = new ArrayList<>();
    methods.addAll(methodsIn(env.getElementsAnnotatedWith(Description.class)));
    methods.addAll(methodsIn(env.getElementsAnnotatedWith(LongName.class)));
    methods.addAll(methodsIn(env.getElementsAnnotatedWith(Positional.class)));
    methods.addAll(methodsIn(env.getElementsAnnotatedWith(ShortName.class)));
    for (ExecutableElement method : methods) {
      Element enclosingElement = method.getEnclosingElement();
      if (enclosingElement.getAnnotation(CommandLineArguments.class) == null) {
        printError(method,
            "The enclosing class " + enclosingElement.getSimpleName() + " must have the " +
                CommandLineArguments.class.getSimpleName() + " annotation");
        return false;
      }
      if (!method.getModifiers().contains(ABSTRACT)) {
        printError(method,
            "Method " + methodToString(method) + " must be abstract.");
        return false;
      }
    }
    return true;
  }

  private void printError(Element element, String message) {
    processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, message, element);
  }
}
