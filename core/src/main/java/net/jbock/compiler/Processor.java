package net.jbock.compiler;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static javax.lang.model.util.ElementFilter.methodsIn;
import static javax.tools.Diagnostic.Kind.ERROR;
import static net.jbock.compiler.LessElements.asType;
import static net.jbock.compiler.OptionType.EVERYTHING_AFTER;
import static net.jbock.compiler.OptionType.FLAG;
import static net.jbock.compiler.OptionType.OTHER_TOKENS;

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
import net.jbock.com.squareup.javapoet.TypeName;
import net.jbock.com.squareup.javapoet.TypeSpec;

public final class Processor extends AbstractProcessor {

  private static final String SUFFIX = "_Parser";

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
    if (missingClassLevelAnnotation(env)) {
      return false;
    }
    List<TypeElement> typeElements = getAnnotatedClasses(env);
    for (TypeElement typeElement : typeElements) {
      try {
        List<Param> params = validate(typeElement);
        String stopword = stopword(params, typeElement);
        Context context = Context.create(typeElement, params, stopword);
        if (!done.add(typeElement.accept(Util.QUALIFIED_NAME, null))) {
          continue;
        }
        TypeSpec typeSpec = Analyser.create(context).analyse();
        write(context.generatedClass, typeSpec);
      } catch (ValidationException e) {
        processingEnv.getMessager().printMessage(e.kind, e.getMessage(), e.about);
      } catch (Exception e) {
        handleException(typeElement, e);
      }
    }
    return false;
  }

  private List<TypeElement> getAnnotatedClasses(RoundEnvironment env) {
    Set<? extends Element> annotated = env.getElementsAnnotatedWith(CommandLineArguments.class);
    List<TypeElement> result = new ArrayList<>();
    Set<TypeElement> typeElements = ElementFilter.typesIn(annotated);
    result.addAll(typeElements);
    return result;
  }

  private void handleException(TypeElement constructor, Exception e) {
    String message = "Unexpected error while processing " +
        ClassName.get(asType(constructor)) +
        ": " + e.getMessage();
    e.printStackTrace();
    processingEnv.getMessager().printMessage(ERROR, message, constructor);
  }

  private void write(ClassName generatedType, TypeSpec typeSpec) throws IOException {
    JavaFile javaFile = JavaFile.builder(generatedType.packageName(), typeSpec)
        .skipJavaLangImports(true)
        .build();
    JavaFileObject sourceFile = processingEnv.getFiler()
        .createSourceFile(generatedType.toString(),
            javaFile.typeSpec.originatingElements.toArray(new Element[0]));
    try (Writer writer = sourceFile.openWriter()) {
      writer.write(javaFile.toString());
    }
  }

  private static ClassName peer(ClassName type, String suffix) {
    String name = String.join("_", type.simpleNames()) + suffix;
    return type.topLevelClassName().peerClass(name);
  }

  static final Set<OptionType> ARGNAME_LESS = EnumSet.of(EVERYTHING_AFTER, OTHER_TOKENS, FLAG);
  private static final Set<OptionType> NAMELESS = EnumSet.of(EVERYTHING_AFTER, OTHER_TOKENS);

  private void combinationChecks(List<Param> params) {
    params.forEach(param -> {
      if (NAMELESS.contains(param.optionType())) {
        checkNotPresent(param.variableElement, asList(LongName.class, ShortName.class));
      }
    });
  }

  private List<Param> validate(TypeElement typeElement) {
    if (typeElement.getKind() == ElementKind.INTERFACE) {
      throw new ValidationException(
          typeElement.getSimpleName() + " must be an abstract class, not an interface",
          typeElement);
    }
    if (!typeElement.getModifiers().contains(Modifier.ABSTRACT)) {
      throw new ValidationException(
          typeElement.getSimpleName() + " must be abstract",
          typeElement);
    }
    if (typeElement.getModifiers().contains(Modifier.PRIVATE)) {
      throw new ValidationException(typeElement.getSimpleName() + " may not be private",
          typeElement);
    }
    if (typeElement.getNestingKind().isNested() &&
        !typeElement.getModifiers().contains(Modifier.STATIC)) {
      throw new ValidationException(
          "The nested class " + typeElement.getSimpleName() + " must be static",
          typeElement);
    }
    List<ExecutableElement> getters = methodsIn(typeElement.getEnclosedElements()).stream()
        .filter(method -> method.getModifiers().contains(Modifier.ABSTRACT))
        .collect(toList());
    getters.stream()
        .filter(method -> !method.getParameters().isEmpty())
        .findFirst()
        .ifPresent(badMethod -> {
          throw new ValidationException(
              "The abstract method must have an empty argument list", badMethod);
        });
    List<Param> params = getters.stream()
        .map(Param::create).collect(toList());
    combinationChecks(params);
    return params;
  }

  private Set<String> shortNames(List<Param> params, TypeElement executableElement) {
    return params.stream()
        .map(Param::shortName)
        .filter(Objects::nonNull)
        .collect(Util.distinctSet(element ->
            new ValidationException(
                "Duplicate shortName: " + element, executableElement)));
  }

  private Set<String> longNames(List<Param> params, TypeElement executableElement) {
    return params.stream()
        .map(Param::longName)
        .filter(Objects::nonNull)
        .collect(Util.distinctSet(element ->
            new ValidationException(
                "Duplicate longName: " + element, executableElement)));
  }

  private String stopword(
      List<Param> params,
      TypeElement executableElement) {
    Set<String> longNames = longNames(params, executableElement);
    Set<String> shortNames = shortNames(params, executableElement);
    List<String> stopwords = params.stream()
        .map(Param::stopword)
        .filter(Objects::nonNull)
        .collect(toList());
    if (stopwords.size() > 1) {
      throw new ValidationException("Only one parameter may have @EverythingAfter", executableElement);
    }
    return stopwords.stream().findAny().map(stopword -> {
      if (stopword.startsWith("-")) {
        if (stopword.startsWith("--") && longNames.contains(stopword.substring(2))) {
          throw new ValidationException(
              "@EverythingAfter coincides with a long option", executableElement);
        }
        if (shortNames.contains(stopword.substring(1))) {
          throw new ValidationException(
              "@EverythingAfter coincides with a short option", executableElement);
        }
      }
      return stopword;
    }).orElse(null);
  }

  private void checkNotPresent(ExecutableElement p, List<Class<? extends Annotation>> namelesss) {
    for (Class<? extends Annotation> nameless : namelesss) {
      if (p.getAnnotation(nameless) != null) {
        throw new ValidationException(
            "@" + nameless.getSimpleName() + " not allowed here", p);
      }
    }
  }

  static final class Context {
    final TypeElement sourceType;
    final ClassName generatedClass;
    final List<Param> parameters;
    final String stopword;
    final boolean otherTokens;
    final boolean rest;

    private Context(
        TypeElement sourceType,
        ClassName generatedClass,
        List<Param> parameters,
        String stopword, boolean otherTokens, boolean rest) {
      this.sourceType = sourceType;
      this.generatedClass = generatedClass;
      this.parameters = parameters;
      this.stopword = stopword;
      this.otherTokens = otherTokens;
      this.rest = rest;
    }

    private static Context create(
        TypeElement sourceType,
        List<Param> parameters,
        String stopword) {
      ClassName generatedClass = peer(ClassName.get(asType(sourceType)), SUFFIX);
      boolean otherTokens = parameters.stream()
          .anyMatch(p -> p.optionType == OptionType.OTHER_TOKENS);
      boolean rest = parameters.stream()
          .anyMatch(p -> p.optionType == OptionType.EVERYTHING_AFTER);
      return new Context(sourceType, generatedClass, parameters, stopword, otherTokens, rest);
    }

    TypeName returnType() {
      return TypeName.get(sourceType.asType());
    }
  }


  private boolean missingClassLevelAnnotation(RoundEnvironment env) {
    List<ExecutableElement> toCheck = new ArrayList<>();
    toCheck.addAll(methodsIn(env.getElementsAnnotatedWith(Description.class)));
    toCheck.addAll(methodsIn(env.getElementsAnnotatedWith(EverythingAfter.class)));
    toCheck.addAll(methodsIn(env.getElementsAnnotatedWith(LongName.class)));
    toCheck.addAll(methodsIn(env.getElementsAnnotatedWith(OtherTokens.class)));
    toCheck.addAll(methodsIn(env.getElementsAnnotatedWith(ShortName.class)));
    toCheck.addAll(methodsIn(env.getElementsAnnotatedWith(SuppressLongName.class)));
    for (ExecutableElement executableElement : toCheck) {
      Element enclosingElement = executableElement.getEnclosingElement();
      if (enclosingElement.getAnnotation(CommandLineArguments.class) == null) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
            "The enclosing class " + enclosingElement.getSimpleName() + " must have the " +
                CommandLineArguments.class.getSimpleName() + " annotation", executableElement);
        return true;
      }
      if (!executableElement.getModifiers().contains(Modifier.ABSTRACT)) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
            "Method " + executableElement.getSimpleName() + " must be abstract.", executableElement);
        return true;
      }
      if (executableElement.getModifiers().contains(Modifier.PRIVATE)) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
            "Method " + executableElement.getSimpleName() + " may not be private.", executableElement);
        return true;
      }
      if (executableElement.getModifiers().contains(Modifier.STATIC)) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
            "Method " + executableElement.getSimpleName() + " may not be static.", executableElement);
        return true;
      }
      if (!executableElement.getParameters().isEmpty()) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
            "Method " + executableElement.getSimpleName() + " must have an empty parameter list.", executableElement);
        return true;
      }
    }
    return false;
  }
}
