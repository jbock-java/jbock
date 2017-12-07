package net.jbock.compiler;

import static com.google.common.truth.Truth.assertAbout;
import static com.google.testing.compile.JavaFileObjects.forSourceLines;
import static com.google.testing.compile.JavaSourcesSubjectFactory.javaSources;
import static java.util.Collections.singletonList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.tools.JavaFileObject;
import org.junit.Test;

public class ProcessorTest {

  @Test
  public void duplicateLongName() {
    List<String> sourceLines = withImports(
        "@CommandLineArguments",
        "abstract class InvalidArguments {",
        "  @LongName(\"x\") abstract String a();",
        "  @LongName(\"x\") abstract String b();",
        "}");
    JavaFileObject javaFile = forSourceLines("test.InvalidArguments", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("Duplicate long name: x");
  }

  @Test
  public void duplicateShortName() {
    List<String> sourceLines = withImports(
        "@CommandLineArguments",
        "abstract class InvalidArguments {",
        "  @ShortName('x') abstract String a();",
        "  @ShortName('x') abstract String b();",
        "}");
    JavaFileObject javaFile = forSourceLines("test.InvalidArguments", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("Duplicate short name: x");
  }

  @Test
  public void unknownReturnType() {
    List<String> sourceLines = withImports(
        "@CommandLineArguments",
        "abstract class InvalidArguments {",
        "  abstract long a();",
        "}");
    JavaFileObject javaFile = forSourceLines("test.InvalidArguments", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("a() returns long");
  }

  @Test
  public void declaredException() {
    List<String> sourceLines = withImports(
        "@CommandLineArguments",
        "abstract class InvalidArguments {",
        "  abstract String a() throws IllegalArgumentException;",
        "}");
    JavaFileObject javaFile = forSourceLines("test.InvalidArguments", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("may not declare any exceptions");
  }

  @Test
  public void classNotAbstract() {
    List<String> sourceLines = withImports(
        "@CommandLineArguments",
        "class InvalidArguments {",
        "  abstract String a();",
        "}");
    JavaFileObject javaFile = forSourceLines("test.InvalidArguments", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("InvalidArguments must be abstract");
  }

  @Test
  public void interfaceNotClass() {
    List<String> sourceLines = withImports(
        "@CommandLineArguments",
        "interface InvalidArguments {",
        "  abstract String a();",
        "}");
    JavaFileObject javaFile = forSourceLines("test.InvalidArguments", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("must be an abstract class, not an interface");
  }

  @Test
  public void whitespaceInName() {
    List<String> sourceLines = withImports(
        "@CommandLineArguments",
        "abstract class InvalidArguments {",
        "  @LongName(\"a \") abstract String a();",
        "}");
    JavaFileObject javaFile = forSourceLines("test.InvalidArguments", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("may not contain whitespace");
  }

  @Test
  public void otherTokensNotList() {
    List<String> sourceLines = withImports(
        "@CommandLineArguments",
        "abstract class InvalidArguments {",
        "  @Positional abstract String a();",
        "}");
    JavaFileObject javaFile = forSourceLines("test.InvalidArguments", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("The method that carries the Positional annotation must return List<String>");
  }

  @Test
  public void otherTokensTwice() {
    List<String> sourceLines = withImports(
        "@CommandLineArguments",
        "abstract class InvalidArguments {",
        "  @Positional abstract List<String> a();",
        "  @Positional abstract List<String> b();",
        "}");
    JavaFileObject javaFile = forSourceLines("test.InvalidArguments", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("Only one method may have the @Positional annotation");
  }

  @Test
  public void missingCommandLineArgumentsAnnotation() {
    List<String> sourceLines = withImports(
        "abstract class InvalidArguments {",
        "  @LongName(\"a\") abstract String a();",
        "}");
    JavaFileObject javaFile = forSourceLines("test.InvalidArguments", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("must have the CommandLineArguments annotation");
  }

  @Test
  public void noNames() {
    List<String> sourceLines = withImports(
        "@CommandLineArguments",
        "abstract class InvalidArguments {",
        "  @SuppressLongName abstract String a();",
        "}");
    JavaFileObject javaFile = forSourceLines("test.InvalidArguments", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("Neither long nor short name defined for method a()");
  }

  @Test
  public void annotatedMethodNotAbstract() {
    List<String> sourceLines = withImports(
        "@CommandLineArguments",
        "abstract class InvalidArguments {",
        "  @ShortName('x') String a();",
        "}");
    JavaFileObject javaFile = forSourceLines("test.InvalidArguments", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("a() must be abstract");
  }

  @Test
  public void abstractMethodHasParameter() {
    List<String> sourceLines = withImports(
        "@CommandLineArguments",
        "abstract class InvalidArguments {",
        "  abstract String a(int b, int c);",
        "}");
    JavaFileObject javaFile = forSourceLines("test.InvalidArguments", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("a(int, int) may not have parameters");
  }

  @Test
  public void warningNoMethods() {
    List<String> sourceLines = withImports(
        "@CommandLineArguments",
        "abstract class InvalidArguments {",
        "}");
    JavaFileObject javaFile = forSourceLines("test.InvalidArguments", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .compilesWithoutError()
        .withWarningContaining("Skipping");
  }

  @Test
  public void warningOnlyOneFlag() {
    List<String> sourceLines = withImports(
        "@CommandLineArguments(allowGrouping = true)",
        "abstract class InvalidArguments {",
        "  abstract String foo();",
        "}");
    JavaFileObject javaFile = forSourceLines("test.InvalidArguments", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .compilesWithoutError()
        .withWarningContaining("less than two flags");
  }

  private List<String> withImports(String... strings) {
    List<String> result = new ArrayList<>(strings.length + 13);
    result.addAll(Arrays.asList(
        "package test;",
        "",
        "import java.util.List;",
        "import java.util.Optional;",
        "",
        "import net.jbock.CommandLineArguments;",
        "import net.jbock.Positional;",
        "import net.jbock.LongName;",
        "import net.jbock.ShortName;",
        "import net.jbock.SuppressLongName;",
        "import net.jbock.Description;",
        ""));
    Collections.addAll(result, strings);
    return result;
  }
}
