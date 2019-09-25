package net.jbock.compiler;

import org.junit.jupiter.api.Test;

import javax.tools.JavaFileObject;
import java.util.List;

import static com.google.common.truth.Truth.assertAbout;
import static com.google.testing.compile.JavaFileObjects.forSourceLines;
import static com.google.testing.compile.JavaSourcesSubjectFactory.javaSources;
import static java.util.Collections.singletonList;
import static net.jbock.compiler.ProcessorTest.withImports;

class PositionalTest {

  @Test
  void simpleOptional() {
    List<String> sourceLines = withImports(
        "@CommandLineArguments",
        "abstract class Arguments {",
        "",
        "  @PositionalParameter",
        "  abstract Optional<String> a();",
        "}");
    JavaFileObject javaFile = forSourceLines("test.Arguments", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .compilesWithoutError();
  }

  @Test
  void mayNotDeclareAsOptionalInt() {
    List<String> sourceLines = withImports(
        "@CommandLineArguments",
        "abstract class Arguments {",
        "  @PositionalParameter abstract Optional<Integer> a();",
        "}");
    JavaFileObject javaFile = forSourceLines("test.Arguments", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .compilesWithoutError();
  }

  @Test
  void positionalOptionalsAnyOrder() {
    List<String> sourceLines = withImports(
        "@CommandLineArguments",
        "abstract class Arguments {",
        "  @PositionalParameter(position = -1) abstract Optional<String> a();",
        "  @PositionalParameter(position = 10) abstract Optional<Integer> b();",
        "  @PositionalParameter(position = 100) abstract Optional<String> c();",
        "  @PositionalParameter(position = 1000) abstract Optional<Integer> d();",
        "}");
    JavaFileObject javaFile = forSourceLines("test.Arguments", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .compilesWithoutError();
  }

  @Test
  void positionalAutomaticPosition() {
    // position can be inferred from rank (optional after required)
    List<String> sourceLines = withImports(
        "@CommandLineArguments",
        "abstract class Arguments {",
        "  @PositionalParameter abstract String a();",
        "  @PositionalParameter(position = 1) abstract Optional<Integer> b();",
        "}");
    JavaFileObject javaFile = forSourceLines("test.Arguments", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .compilesWithoutError();
  }

  @Test
  void positionalConflict() {
    // two positionals with same rank (both required) -> position must be specified
    List<String> sourceLines = withImports(
        "@CommandLineArguments",
        "abstract class Arguments {",
        "  @PositionalParameter abstract int a();",
        "  @PositionalParameter abstract int b();",
        "}");
    JavaFileObject javaFile = forSourceLines("test.Arguments", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("Define a unique position.");
  }

  @Test
  void positionalNonzeroNoInfer() {
    // there is a non-zero position -> all positions must be explicit and unique
    List<String> sourceLines = withImports(
        "@CommandLineArguments",
        "abstract class Arguments {",
        "  @PositionalParameter abstract String a();",
        "  @PositionalParameter abstract OptionalInt b();",
        "  @PositionalParameter(position = 1) abstract Optional<String> c();",
        "}");
    JavaFileObject javaFile = forSourceLines("test.Arguments", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("Define a unique position.");
  }


  @Test
  void positionalBadReturnTypeStringBuilder() {
    List<String> sourceLines = withImports(
        "@CommandLineArguments",
        "abstract class Arguments {",
        "  @PositionalParameter abstract StringBuilder a();",
        "}");
    JavaFileObject javaFile = forSourceLines("test.Arguments", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("Unknown parameter type. Define a custom mapper.");
  }


  @Test
  void invalidPositionalBooleanObject() {
    List<String> sourceLines = withImports(
        "@CommandLineArguments",
        "abstract class Arguments {",
        "  @PositionalParameter abstract Boolean a();",
        "}");
    JavaFileObject javaFile = forSourceLines("test.Arguments", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("Unknown parameter type. Define a custom mapper.");
  }

  @Test
  void invalidPositionalBooleanPrimitive() {
    List<String> sourceLines = withImports(
        "@CommandLineArguments",
        "abstract class Arguments {",
        "  @PositionalParameter abstract boolean a();",
        "}");
    JavaFileObject javaFile = forSourceLines("test.Arguments", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("Unknown parameter type. Define a custom mapper.");
  }

  @Test
  void positionalAllRanks() {
    List<String> sourceLines = withImports(
        "@CommandLineArguments",
        "abstract class Arguments {",
        "  @PositionalParameter abstract String b();",
        "  @PositionalParameter(position = 1) abstract Optional<String> c();",
        "  @PositionalParameter(position = 2) abstract List<String> a();",
        "}");
    JavaFileObject javaFile = forSourceLines("test.Arguments", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .compilesWithoutError();
  }

  @Test
  void twoPositionalLists() {
    List<String> sourceLines = withImports(
        "@CommandLineArguments",
        "abstract class Arguments {",
        "  @PositionalParameter(position = 1) abstract List<String> a();",
        "  @PositionalParameter(position = 2) abstract List<String> b();",
        "}");
    JavaFileObject javaFile = forSourceLines("test.Arguments", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("There can only be one one repeatable positional parameter.");
  }

  @Test
  void validList() {
    List<String> sourceLines = withImports(
        "@CommandLineArguments",
        "abstract class Arguments {",
        "  @PositionalParameter abstract List<String> a();",
        "}");
    JavaFileObject javaFile = forSourceLines("test.Arguments", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .compilesWithoutError();
  }

  @Test
  void positionalOptionalBeforeRequired() {
    List<String> sourceLines = withImports(
        "@CommandLineArguments",
        "abstract class Arguments {",
        "  @PositionalParameter(position = 0) abstract Optional<String> a();",
        "  @PositionalParameter(position = 1) abstract String b();",
        "}");
    JavaFileObject javaFile = forSourceLines("test.Arguments", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("Invalid position");
  }

  @Test
  void positionalListBeforeOptional() {
    List<String> sourceLines = withImports(
        "@CommandLineArguments",
        "abstract class Arguments {",
        "  @PositionalParameter(position = 0) abstract List<String> a();",
        "  @PositionalParameter(position = 1) abstract Optional<String> b();",
        "}");
    JavaFileObject javaFile = forSourceLines("test.Arguments", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("Invalid position");
  }

  @Test
  void positionalListBeforeRequired() {
    List<String> sourceLines = withImports(
        "@CommandLineArguments",
        "abstract class Arguments {",
        "  @PositionalParameter(position = 0) abstract List<String> a();",
        "  @PositionalParameter(position = 1) abstract String b();",
        "}");
    JavaFileObject javaFile = forSourceLines("test.Arguments", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("Invalid position");
  }

  @Test
  void mayNotDeclareAsOptional() {
    List<String> sourceLines = withImports(
        "@CommandLineArguments",
        "abstract class Arguments {",
        "  @PositionalParameter abstract Optional<Integer> a();",
        "}");
    JavaFileObject javaFile = forSourceLines("test.Arguments", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .compilesWithoutError();
  }
}
