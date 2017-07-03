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

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import java.io.IOException;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
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

  private final Set<TypeName> done = new HashSet<>();

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
    List<ExecutableElement> constructors =
        getAnnotatedExecutableElements(env);
    validate(constructors);
    for (ExecutableElement c : constructors) {
      try {
        String stopword = staticChecks(c);
        staticChecks(LessElements.asType(c.getEnclosingElement()));
        Context context = Context.create(c, stopword);
        if (!done.add(context.enclosingType)) {
          continue;
        }
        TypeSpec typeSpec = Analyser.create(context).analyse();
        write(context.generatedClass, typeSpec);
      } catch (ValidationException e) {
        processingEnv.getMessager().printMessage(e.kind, e.getMessage(), e.about);
      } catch (Exception e) {
        handleException(c, e);
        return false;
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

  private void validate(Collection<ExecutableElement> constructors) {
    Set<String> check = new HashSet<>();
    List<TypeElement> t = constructors.stream()
        .map(ExecutableElement::getEnclosingElement)
        .map(Element::asType)
        .map(LessTypes::asTypeElement)
        .collect(toList());
    for (TypeElement typeElement : t) {
      if (!check.add(typeElement.getQualifiedName().toString())) {
        processingEnv.getMessager().printMessage(ERROR,
            CommandLineArguments.class.getSimpleName() + " can only appear once per class",
            typeElement);
      }
    }
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

  private void staticChecks(TypeElement enclosingElement) {
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

  private String staticChecks(ExecutableElement executableElement) {
    if (executableElement.getModifiers().contains(Modifier.PRIVATE)) {
      throw new ValidationException(String.format("The %s may not be private",
          executableElement.getKind().name().toLowerCase(Locale.ENGLISH)), executableElement);
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
                String.format("Class '%s' may not be private", typeElement.getSimpleName())
                , executableElement);
          }
        });
    List<? extends VariableElement> parameters = executableElement.getParameters();
    Set<String> shortNames = new HashSet<>();
    Set<String> longNames = new HashSet<>();
    boolean[] otherTokensFound = new boolean[1];
    String[] stopword = new String[1];
    parameters.forEach(p -> {
      Param param = Param.create(p);
      if (NAMELESS.contains(param.optionType())) {
        checkNotPresent(p, asList(LongName.class, ShortName.class));
      }
      if (ARGNAME_LESS.contains(param.optionType())) {
        checkNotPresent(p, singletonList(ArgumentName.class));
      }
      if (param.longName() != null && !longNames.add(param.longName())) {
        throw new ValidationException(
            "Duplicate longName: " + param.longName(), p);
      }
      if (param.shortName() != null && !shortNames.add(param.shortName())) {
        throw new ValidationException(
            "Duplicate shortName: " + param.shortName(), p);
      }
      if (param.optionType() == OTHER_TOKENS) {
        if (otherTokensFound[0]) {
          throw new ValidationException(
              "Only one parameter may have @OtherTokens", p);
        }
        otherTokensFound[0] = true;
      }
      if (param.optionType() == EVERYTHING_AFTER) {
        if (stopword[0] != null) {
          throw new ValidationException(
              "Only one parameter may have @EverythingAfter", p);
        }
        stopword[0] = param.stopword;
      }
    });
    if (stopword[0] != null && stopword[0].startsWith("-")) {
      if (stopword[0].startsWith("--") && longNames.contains(stopword[0].substring(2))) {
        throw new ValidationException(
            "@EverythingAfter coincides with a long option", executableElement);
      }
      if (shortNames.contains(stopword[0].substring(1))) {
        throw new ValidationException(
            "@EverythingAfter coincides with a short option", executableElement);
      }
    }
    return stopword[0];
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
        String stopword) {
      List<TypeName> thrownTypes = executableElement.getThrownTypes().stream().map(TypeName::get).collect(toList());
      TypeName enclosingType = TypeName.get(executableElement.getEnclosingElement().asType());
      List<Param> parameters = executableElement.getParameters().stream()
          .map(Param::create)
          .collect(Collectors.toList());
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
