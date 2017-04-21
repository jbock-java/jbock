package net.jbock.compiler;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import net.jbock.ArgumentName;
import net.jbock.CommandLineArguments;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static javax.lang.model.util.ElementFilter.constructorsIn;
import static javax.tools.Diagnostic.Kind.ERROR;
import static net.jbock.compiler.LessElements.asType;
import static net.jbock.compiler.OptionType.OTHER_TOKENS;

public final class Processor extends AbstractProcessor {

  private static final String SUFFIX = "Parser";

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
    Set<ExecutableElement> constructors =
        constructorsIn(env.getElementsAnnotatedWith(CommandLineArguments.class));
    validate(constructors);
    for (ExecutableElement c : constructors) {
      try {
        staticChecks(c);
        staticChecks(LessElements.asType(c.getEnclosingElement()));
        Constructor constructor = Constructor.create(c);
        if (!done.add(constructor.enclosingType)) {
          continue;
        }
        TypeSpec typeSpec = Analyser.create(constructor).analyse();
        write(constructor.generatedClass, typeSpec);
      } catch (ValidationException e) {
        processingEnv.getMessager().printMessage(e.kind, e.getMessage(), e.about);
      } catch (Exception e) {
        handleException(c, e);
        return false;
      }
    }
    return false;
  }

  private void handleException(ExecutableElement enclosingElement, Exception e) {
    e.printStackTrace();
    String message = "Error processing " +
        ClassName.get(asType(enclosingElement.getEnclosingElement())) +
        ": " + e.getMessage();
    processingEnv.getMessager().printMessage(ERROR, message, enclosingElement);
  }

  private void validate(Set<ExecutableElement> constructors) {
    Set<TypeElement> check = new HashSet<>();
    List<TypeElement> t = constructors.stream()
        .map(ExecutableElement::getEnclosingElement)
        .map(Element::asType)
        .map(LessTypes::asTypeElement)
        .collect(toList());
    for (TypeElement typeElement : t) {
      if (!check.add(typeElement)) {
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
      throw new ValidationException(ERROR, "The class may not be private", enclosingElement);
    }
    if (enclosingElement.getNestingKind().isNested() &&
        !enclosingElement.getModifiers().contains(Modifier.STATIC)) {
      throw new ValidationException(ERROR, "The inner class must be static", enclosingElement);
    }
  }

  static final Set<OptionType> ARGLESS = EnumSet.of(OptionType.FLAG, OTHER_TOKENS);

  private void staticChecks(ExecutableElement constructor) {
    if (constructor.getModifiers().contains(Modifier.PRIVATE)) {
      throw new ValidationException(ERROR, "The constructor may not be private", constructor);
    }
    List<? extends VariableElement> parameters = constructor.getParameters();
    Set<Character> checkShort = new HashSet<>();
    Set<String> checkLong = new HashSet<>();
    boolean[] remainingFound = new boolean[1];
    parameters.forEach(p -> {
      Names names = Names.create(p);
      if (ARGLESS.contains(names.optionType) && p.getAnnotation(ArgumentName.class) != null) {
        throw new ValidationException(Diagnostic.Kind.ERROR,
            "@ArgumentName not allowed here", p);
      }
      if (names.longName != null && !checkLong.add(names.longName)) {
        throw new ValidationException(Diagnostic.Kind.ERROR,
            "Duplicate longName: " + names.longName, p);
      }
      if (names.shortName != ' ' && !checkShort.add(names.shortName)) {
        throw new ValidationException(Diagnostic.Kind.ERROR,
            "Duplicate shortName: " + names.shortName, p);
      }
      if (names.optionType == OptionType.OTHER_TOKENS) {
        if (remainingFound[0]) {
          throw new ValidationException(Diagnostic.Kind.ERROR,
              "Only one parameter may have @OtherTokens", p);
        }
        remainingFound[0] = true;
      }
    });
  }

  static final class Constructor {
    final TypeName enclosingType;
    final ClassName generatedClass;
    final List<Names> parameters;
    final List<TypeName> thrownTypes;

    private Constructor(TypeName enclosingType, ClassName generatedClass, List<Names> parameters, List<TypeName> thrownTypes) {
      this.enclosingType = enclosingType;
      this.generatedClass = generatedClass;
      this.parameters = parameters;
      this.thrownTypes = thrownTypes;
    }

    private static Constructor create(ExecutableElement executableElement) {
      List<TypeName> thrownTypes = executableElement.getThrownTypes().stream().map(TypeName::get).collect(toList());
      TypeName enclosingType = TypeName.get(executableElement.getEnclosingElement().asType());
      List<Names> parameters = executableElement.getParameters().stream()
          .map(Names::create)
          .collect(Collectors.toList());
      ClassName generatedClass = peer(ClassName.get(asType(executableElement.getEnclosingElement())), SUFFIX);
      return new Constructor(enclosingType, generatedClass, parameters, thrownTypes);
    }
  }
}