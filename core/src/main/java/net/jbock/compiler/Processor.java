package net.jbock.compiler;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static javax.lang.model.util.ElementFilter.methodsIn;
import static net.jbock.compiler.Type.EVERYTHING_AFTER;
import static net.jbock.compiler.Util.asType;
import static net.jbock.compiler.Util.methodToString;

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
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import net.jbock.CommandLineArguments;
import net.jbock.Description;
import net.jbock.EverythingAfter;
import net.jbock.LongName;
import net.jbock.OtherTokens;
import net.jbock.ShortName;
import net.jbock.SuppressLongName;
import net.jbock.com.squareup.javapoet.ClassName;
import net.jbock.com.squareup.javapoet.JavaFile;
import net.jbock.com.squareup.javapoet.TypeSpec;

public final class Processor extends AbstractProcessor {

  private final Set<String> done = new HashSet<>();

  @Override
  public Set<String> getSupportedAnnotationTypes() {
    return Stream.of(
        CommandLineArguments.class,
        Description.class,
        EverythingAfter.class,
        LongName.class,
        OtherTokens.class,
        ShortName.class,
        SuppressLongName.class)
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
    List<TypeElement> typeElements = getAnnotatedClasses(env);
    for (TypeElement sourceType : typeElements) {
      try {
        List<Param> parameters = validate(sourceType);
        if (parameters.isEmpty()) {
          processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING,
              "Skipping code generation: No abstract methods found", sourceType);
          continue;
        }
        String stopword = stopword(parameters, sourceType);
        Set<Type> paramTypes = paramTypes(parameters);
        long numFlags = parameters.stream()
            .filter(p -> p.paramType == Type.FLAG)
            .count();
        boolean groupingRequested = sourceType.getAnnotation(CommandLineArguments.class).grouping();
        boolean grouping = groupingRequested && numFlags >= 2;
        if (groupingRequested && !grouping) {
          processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING,
              "Grouping requested, but less than two flags defined", sourceType);
        }
        Context context = Context.create(sourceType, parameters, stopword, paramTypes, grouping);
        if (!done.add(asType(sourceType).getQualifiedName().toString())) {
          continue;
        }
        TypeSpec typeSpec = Parser.create(context).define();
        boolean onlyPrimitives = parameters.stream()
            .allMatch(p -> p.paramType.returnType.isPrimitive());
        write(onlyPrimitives, context.generatedClass, typeSpec);
      } catch (ValidationException e) {
        processingEnv.getMessager().printMessage(e.kind, e.getMessage(), e.about);
      } catch (Exception e) {
        handleException(sourceType, e);
      }
    }
    return false;
  }

  private static Set<Type> paramTypes(List<Param> parameters) {
    Set<Type> paramTypes = EnumSet.noneOf(Type.class);
    parameters.forEach(p -> paramTypes.add(p.paramType));
    return paramTypes;
  }

  private List<TypeElement> getAnnotatedClasses(RoundEnvironment env) {
    Set<? extends Element> annotated = env.getElementsAnnotatedWith(CommandLineArguments.class);
    Set<TypeElement> typeElements = ElementFilter.typesIn(annotated);
    return new ArrayList<>(typeElements);
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
      boolean onlyPrimitive,
      ClassName generatedType,
      TypeSpec typeSpec) throws IOException {
    JavaFile.Builder builder = JavaFile.builder(generatedType.packageName(), typeSpec);
    if (!onlyPrimitive) {
      builder.addStaticImport(Objects.class, "requireNonNull");
    }
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

  private void checkSpecialParams(List<Param> params) {
    List<Param> otherTokens = params.stream()
        .filter(param -> param.paramType == Type.OTHER_TOKENS)
        .collect(toList());
    if (otherTokens.size() > 1) {
      throw new ValidationException(params.get(1).sourceMethod,
          "Only one method may have the @OtherTokens annotation");
    }
  }

  private List<Param> validate(TypeElement typeElement) {
    if (typeElement.getKind() == ElementKind.INTERFACE) {
      throw new ValidationException(typeElement,
          typeElement.getSimpleName() + " must be an abstract class, not an interface");
    }
    if (!typeElement.getModifiers().contains(Modifier.ABSTRACT)) {
      throw new ValidationException(typeElement,
          typeElement.getSimpleName() + " must be abstract");
    }
    if (typeElement.getModifiers().contains(Modifier.PRIVATE)) {
      throw new ValidationException(typeElement,
          typeElement.getSimpleName() + " may not be private");
    }
    if (typeElement.getNestingKind().isNested() &&
        !typeElement.getModifiers().contains(Modifier.STATIC)) {
      throw new ValidationException(typeElement,
          "The nested class " + typeElement.getSimpleName() + " must be static");
    }
    List<Param> params = methodsIn(typeElement.getEnclosedElements()).stream()
        .filter(method -> method.getModifiers().contains(Modifier.ABSTRACT))
        .map(Param::create).collect(toList());
    checkSpecialParams(params);
    return params;
  }

  private Set<String> shortNames(List<Param> params, TypeElement sourceType) {
    return params.stream()
        .map(Param::shortName)
        .filter(Objects::nonNull)
        .collect(Util.distinctSet(element ->
            new ValidationException(sourceType,
                "Duplicate short name: " + element)));
  }

  private Set<String> longNames(List<Param> params, TypeElement sourceType) {
    return params.stream()
        .map(Param::longName)
        .filter(Objects::nonNull)
        .collect(Util.distinctSet(element ->
            new ValidationException(sourceType,
                "Duplicate long name: " + element)));
  }

  private String stopword(
      List<Param> params,
      TypeElement sourceType) {
    Set<String> longNames = longNames(params, sourceType);
    Set<String> shortNames = shortNames(params, sourceType);
    List<String> stopwords = params.stream()
        .filter(param -> param.paramType == EVERYTHING_AFTER)
        .map(Param::stopword)
        .collect(toList());
    if (stopwords.size() > 1) {
      throw new ValidationException(sourceType,
          "Only one method may have the @EverythingAfter annotation");
    }
    if (stopwords.isEmpty()) {
      return null;
    }
    String stopword = stopwords.get(0);
    if (stopword.charAt(0) != '-') {
      return stopword;
    }
    if (shortNames.contains(stopword.substring(1))) {
      throw new ValidationException(sourceType,
          "stopword coincides with an option: " + stopword);
    }
    if (stopword.length() == 1) {
      return stopword;
    }
    if (stopword.charAt(1) == '-' && longNames.contains(stopword.substring(2))) {
      throw new ValidationException(sourceType,
          "stopword coincides with an option: " + stopword);
    }
    return stopword;
  }

  static void checkNotPresent(
      ExecutableElement executableElement,
      Annotation cause,
      List<Class<? extends Annotation>> forbiddenAnnotations) {
    for (Class<? extends Annotation> annotation : forbiddenAnnotations) {
      if (executableElement.getAnnotation(annotation) != null) {
        throw new ValidationException(executableElement,
            "@" + annotation.getSimpleName() +
                " is conflicting with @" + cause.annotationType().getSimpleName());
      }
    }
  }

  private boolean checkValid(RoundEnvironment env) {
    List<ExecutableElement> methods = new ArrayList<>();
    methods.addAll(methodsIn(env.getElementsAnnotatedWith(Description.class)));
    methods.addAll(methodsIn(env.getElementsAnnotatedWith(EverythingAfter.class)));
    methods.addAll(methodsIn(env.getElementsAnnotatedWith(LongName.class)));
    methods.addAll(methodsIn(env.getElementsAnnotatedWith(OtherTokens.class)));
    methods.addAll(methodsIn(env.getElementsAnnotatedWith(ShortName.class)));
    methods.addAll(methodsIn(env.getElementsAnnotatedWith(SuppressLongName.class)));
    for (ExecutableElement method : methods) {
      Element enclosingElement = method.getEnclosingElement();
      if (enclosingElement.getAnnotation(CommandLineArguments.class) == null) {
        printError(method,
            "The enclosing class " + enclosingElement.getSimpleName() + " must have the " +
                CommandLineArguments.class.getSimpleName() + " annotation");
        return false;
      }
      if (!method.getModifiers().contains(Modifier.ABSTRACT)) {
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
