package net.jbock.compiler;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;
import net.jbock.Argument;
import net.jbock.CommandLineArguments;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
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
import static net.jbock.compiler.Analyser.analyse;
import static net.jbock.compiler.LessElements.asType;

public final class Processor extends AbstractProcessor {

  static final String SUFFIX = "Parser";

  private final Set<ExecutableElement> done = new HashSet<>();

  @Override
  public Set<String> getSupportedAnnotationTypes() {
    return Stream.of(Argument.class, CommandLineArguments.class)
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
    for (ExecutableElement enclosingElement : constructors) {
      try {
        if (!done.add(enclosingElement)) {
          continue;
        }
        TypeSpec typeSpec = analyse(enclosingElement);
        ClassName generatedClass = peer(ClassName.get(asType(enclosingElement.getEnclosingElement())), SUFFIX);
        write(generatedClass, typeSpec);
      } catch (ValidationException e) {
        processingEnv.getMessager().printMessage(e.kind, e.getMessage(), e.about);
      } catch (Exception e) {
        handleException(enclosingElement, e);
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
}