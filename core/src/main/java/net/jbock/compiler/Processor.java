package net.jbock.compiler;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static javax.lang.model.util.ElementFilter.constructorsIn;
import static javax.tools.Diagnostic.Kind.ERROR;
import static net.jbock.compiler.LessElements.asType;

public final class Processor extends AbstractProcessor {

  private static final String SUFFIX = "Parser";

  private final Set<ExecutableElement> done = new HashSet<>();

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
    for (ExecutableElement constructor : constructors) {
      try {
        if (!done.add(constructor)) {
          continue;
        }
        staticChecks(constructor);
        staticChecks(LessElements.asType(constructor.getEnclosingElement()));
        ClassName generatedClass = peer(ClassName.get(asType(constructor.getEnclosingElement())), SUFFIX);
        Analyser analyser = new Analyser(constructor, generatedClass);
        TypeSpec typeSpec = analyser.analyse();
        write(generatedClass, typeSpec);
      } catch (ValidationException e) {
        processingEnv.getMessager().printMessage(e.kind, e.getMessage(), e.about);
      } catch (Exception e) {
        handleException(constructor, e);
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

  private void staticChecks(ExecutableElement constructor) {
    if (constructor.getModifiers().contains(Modifier.PRIVATE)) {
      throw new ValidationException(ERROR, "The constructor may not be private", constructor);
    }
    List<? extends VariableElement> parameters = constructor.getParameters();
    Set<Character> checkShort = new HashSet<>();
    Set<String> checkLong = new HashSet<>();
    parameters.forEach(p -> {
      Names names = Names.create(p);
      if (names.longName != null && !checkLong.add(names.longName)) {
        throw new ValidationException(Diagnostic.Kind.ERROR,
            "Duplicate longName: " + names.longName, p);
      }
      if (names.shortName != ' ' && !checkShort.add(names.shortName)) {
        throw new ValidationException(Diagnostic.Kind.ERROR,
            "Duplicate shortName: " + names.shortName, p);
      }
    });
  }
}