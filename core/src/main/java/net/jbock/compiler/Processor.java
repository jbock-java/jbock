package net.jbock.compiler;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static javax.lang.model.util.ElementFilter.constructorsIn;
import static javax.lang.model.util.ElementFilter.methodsIn;
import static javax.tools.Diagnostic.Kind.ERROR;
import static net.jbock.compiler.LessElements.asType;
import static net.jbock.compiler.OptionType.EVERYTHING_AFTER;
import static net.jbock.compiler.OptionType.FLAG;
import static net.jbock.compiler.OptionType.OTHER_TOKENS;

import net.jbock.com.squareup.javapoet.ClassName;
import net.jbock.com.squareup.javapoet.JavaFile;
import net.jbock.com.squareup.javapoet.TypeName;
import net.jbock.com.squareup.javapoet.TypeSpec;
import java.io.IOException;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.tools.JavaFileObject;
import net.jbock.ArgumentName;
import net.jbock.CommandLineArguments;
import net.jbock.LongName;
import net.jbock.ShortName;

public final class Processor extends AbstractProcessor {

  private static final String SUFFIX = "_Parser";

  private final Set<String> done = new HashSet<>();

  @Override
  public Set<String> getSupportedAnnotationTypes() {
    return Stream.of(
        CommandLineArguments.class)
        .map(Class::getName)
        .collect(toSet());
  }

  @Override
  public SourceVersion getSupportedSourceVersion() {
    return SourceVersion.latestSupported();
  }

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment env) {
    for (ExecutableElement executableElement : validate(env)) {
      try {
        List<Param> params = validate(executableElement);
        String stopword = stopword(params, executableElement);
        Context context = Context.create(executableElement, params, stopword);
        if (!done.add(executableElement.getEnclosingElement()
            .accept(Util.QUALIFIED_NAME, null))) {
          continue;
        }
        TypeSpec typeSpec = Analyser.create(context).analyse();
        write(context.generatedClass, typeSpec);
      } catch (ValidationException e) {
        processingEnv.getMessager().printMessage(e.kind, e.getMessage(), e.about);
      } catch (Exception e) {
        handleException(executableElement, e);
      }
    }
    return false;
  }

  private List<ExecutableElement> getAnnotatedExecutableElements(RoundEnvironment env) {
    Set<? extends Element> annotated = env.getElementsAnnotatedWith(CommandLineArguments.class);
    List<ExecutableElement> result = new ArrayList<>();
    result.addAll(constructorsIn(annotated));
    result.addAll(methodsIn(annotated));
    return result;
  }

  private void handleException(ExecutableElement constructor, Exception e) {
    String message = "Unexpected error while processing " +
        ClassName.get(asType(constructor.getEnclosingElement())) +
        ": " + e.getMessage();
    e.printStackTrace();
    processingEnv.getMessager().printMessage(ERROR, message, constructor);
  }

  private Collection<ExecutableElement> validate(RoundEnvironment env) {
    List<ExecutableElement> executableElements =
        getAnnotatedExecutableElements(env);
    Map<String, ExecutableElement> check = new HashMap<>();
    for (ExecutableElement executableElement : executableElements) {
      TypeElement typeElement = executableElement.getEnclosingElement()
          .accept(Util.AS_TYPE_ELEMENT, null);
      if (check.put(typeElement.getQualifiedName().toString(),
          executableElement) != null) {
        processingEnv.getMessager().printMessage(ERROR,
            CommandLineArguments.class.getSimpleName() + " can only appear once per class",
            typeElement);
      }
    }
    return check.values();
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

  private void classChecks(TypeElement enclosingElement) {
    if (enclosingElement.getModifiers().contains(Modifier.PRIVATE)) {
      throw new ValidationException("The class may not be private", enclosingElement);
    }
    if (enclosingElement.getNestingKind().isNested() &&
        !enclosingElement.getModifiers().contains(Modifier.STATIC)) {
      throw new ValidationException("The inner class must be static", enclosingElement);
    }
  }

  static final Set<OptionType> ARGNAME_LESS = EnumSet.of(EVERYTHING_AFTER, OTHER_TOKENS, FLAG);
  private static final Set<OptionType> NAMELESS = EnumSet.of(EVERYTHING_AFTER, OTHER_TOKENS);

  private void combinationChecks(List<Param> params) {
    params.forEach(param -> {
      if (NAMELESS.contains(param.optionType())) {
        checkNotPresent(param.variableElement, asList(LongName.class, ShortName.class));
      }
      if (ARGNAME_LESS.contains(param.optionType())) {
        checkNotPresent(param.variableElement, singletonList(ArgumentName.class));
      }
    });
  }

  private List<Param> validate(ExecutableElement executableElement) {
    if (executableElement.getModifiers().contains(Modifier.PRIVATE)) {
      throw new ValidationException(String.format("The %s may not be private",
          executableElement.getKind().name().toLowerCase(Locale.US)), executableElement);
    }
    if (executableElement.getKind() == ElementKind.METHOD &&
        !executableElement.getModifiers().contains(Modifier.STATIC)) {
      throw new ValidationException("The method must be static", executableElement);
    }
    if (executableElement.getKind() == ElementKind.METHOD &&
        executableElement.getReturnType().getKind() == TypeKind.VOID) {
      throw new ValidationException("The method may not return void", executableElement);
    }
    executableElement.getThrownTypes()
        .forEach(t -> {
          TypeElement typeElement = processingEnv.getElementUtils().getTypeElement(t.toString());
          if (typeElement.getModifiers().contains(Modifier.PRIVATE)) {
            throw new ValidationException(
                String.format("Class '%s' may not be private", typeElement.getSimpleName()),
                executableElement);
          }
        });
    classChecks(asType(executableElement.getEnclosingElement()));
    List<Param> params = executableElement.getParameters().stream()
        .map(Param::create).collect(toList());
    combinationChecks(params);
    return params;
  }

  private Set<String> shortNames(List<Param> params, ExecutableElement executableElement) {
    return params.stream()
        .map(Param::shortName)
        .filter(Objects::nonNull)
        .collect(Util.distinctSet(element ->
            new ValidationException(
                "Duplicate shortName: " + element, executableElement)));
  }

  private Set<String> longNames(List<Param> params, ExecutableElement executableElement) {
    return params.stream()
        .map(Param::longName)
        .filter(Objects::nonNull)
        .collect(Util.distinctSet(element ->
            new ValidationException(
                "Duplicate longName: " + element, executableElement)));
  }

  private String stopword(
      List<Param> params,
      ExecutableElement executableElement) {
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

  private void checkNotPresent(VariableElement p, List<Class<? extends Annotation>> namelesss) {
    for (Class<? extends Annotation> nameless : namelesss) {
      if (p.getAnnotation(nameless) != null) {
        throw new ValidationException(
            "@" + nameless.getSimpleName() + " not allowed here", p);
      }
    }
  }

  static final class Context {
    final TypeName enclosingType;
    final ClassName generatedClass;
    final List<Param> parameters;
    final List<TypeName> thrownTypes;
    final String stopword;
    final ExecutableElement executableElement;

    private Context(
        TypeName enclosingType,
        ClassName generatedClass,
        List<Param> parameters,
        List<TypeName> thrownTypes,
        String stopword,
        ExecutableElement executableElement) {
      this.enclosingType = enclosingType;
      this.generatedClass = generatedClass;
      this.parameters = parameters;
      this.thrownTypes = thrownTypes;
      this.stopword = stopword;
      this.executableElement = executableElement;
    }

    private static Context create(
        ExecutableElement executableElement,
        List<Param> parameters,
        String stopword) {
      List<TypeName> thrownTypes = executableElement.getThrownTypes().stream().map(TypeName::get).collect(toList());
      TypeName enclosingType = TypeName.get(executableElement.getEnclosingElement().asType());
      ClassName generatedClass = peer(ClassName.get(asType(executableElement.getEnclosingElement())), SUFFIX);
      return new Context(enclosingType, generatedClass, parameters, thrownTypes, stopword, executableElement);
    }

    TypeName returnType() {
      return executableElement.getKind() == ElementKind.CONSTRUCTOR ?
          enclosingType :
          TypeName.get(executableElement.getReturnType());
    }
  }
}
