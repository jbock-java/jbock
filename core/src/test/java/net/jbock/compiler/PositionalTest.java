package net.jbock.compiler;

import org.junit.jupiter.api.Test;

import javax.tools.JavaFileObject;

import static com.google.common.truth.Truth.assertAbout;
import static com.google.testing.compile.JavaSourcesSubjectFactory.javaSources;
import static java.util.Collections.singletonList;
import static net.jbock.compiler.ProcessorTest.fromSource;

class PositionalTest {

  @Test
  void simpleOptional() {
    JavaFileObject javaFile = fromSource(
        "@CommandLineArguments",
        "abstract class Arguments {",
        "",
        "  @PositionalParameter",
        "  abstract Optional<String> a();",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .compilesWithoutError();
  }

  @Test
  void mayNotDeclareAsOptionalInt() {
    JavaFileObject javaFile = fromSource(
        "@CommandLineArguments",
        "abstract class Arguments {",
        "  @PositionalParameter abstract Optional<Integer> a();",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .compilesWithoutError();
  }

  @Test
  void positionalOptionalsAnyOrder() {
    JavaFileObject javaFile = fromSource(
        "@CommandLineArguments",
        "abstract class Arguments {",
        "  @PositionalParameter(position = -1) abstract Optional<String> a();",
        "  @PositionalParameter(position = 10) abstract Optional<Integer> b();",
        "  @PositionalParameter(position = 100) abstract Optional<String> c();",
        "  @PositionalParameter(position = 1000) abstract Optional<Integer> d();",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .compilesWithoutError();
  }

  @Test
  void positionalAutomaticPosition() {
    JavaFileObject javaFile = fromSource(
        "@CommandLineArguments",
        "abstract class Arguments {",
        "  @PositionalParameter abstract String a();",
        "  @PositionalParameter(position = 1) abstract Optional<Integer> b();",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .compilesWithoutError();
  }

  @Test
  void positionalConflict() {
    JavaFileObject javaFile = fromSource(
        "@CommandLineArguments",
        "abstract class Arguments {",
        "  @PositionalParameter abstract int a();",
        "  @PositionalParameter abstract int b();",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("Define a unique position.");
  }

  @Test
  void positionalNonzeroNoInfer() {
    JavaFileObject javaFile = fromSource(
        "@CommandLineArguments",
        "abstract class Arguments {",
        "  @PositionalParameter abstract String a();",
        "  @PositionalParameter abstract OptionalInt b();",
        "  @PositionalParameter(position = 1) abstract Optional<String> c();",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("Define a unique position.");
  }


  @Test
  void positionalBadReturnTypeStringBuilder() {
    JavaFileObject javaFile = fromSource(
        "@CommandLineArguments",
        "abstract class Arguments {",
        "  @PositionalParameter abstract StringBuilder a();",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("Unknown parameter type. Define a custom mapper.");
  }


  @Test
  void invalidPositionalBooleanObject() {
    JavaFileObject javaFile = fromSource(
        "@CommandLineArguments",
        "abstract class Arguments {",
        "  @PositionalParameter abstract Boolean a();",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("Unknown parameter type. Define a custom mapper.");
  }

  @Test
  void invalidPositionalBooleanPrimitive() {
    JavaFileObject javaFile = fromSource(
        "@CommandLineArguments",
        "abstract class Arguments {",
        "  @PositionalParameter abstract boolean a();",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("Unknown parameter type. Define a custom mapper.");
  }

  @Test
  void positionalAllRanks() {
    JavaFileObject javaFile = fromSource(
        "@CommandLineArguments",
        "abstract class Arguments {",
        "  @PositionalParameter abstract String b();",
        "  @PositionalParameter(position = 1) abstract Optional<String> c();",
        "  @PositionalParameter(position = 2) abstract List<String> a();",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .compilesWithoutError();
  }

  @Test
  void twoPositionalLists() {
    JavaFileObject javaFile = fromSource(
        "@CommandLineArguments",
        "abstract class Arguments {",
        "  @PositionalParameter(position = 1) abstract List<String> a();",
        "  @PositionalParameter(position = 2) abstract List<String> b();",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("There can only be one one repeatable positional parameter.");
  }

  @Test
  void validList() {
    JavaFileObject javaFile = fromSource(
        "@CommandLineArguments",
        "abstract class Arguments {",
        "  @PositionalParameter abstract List<String> a();",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .compilesWithoutError();
  }

  @Test
  void positionalOptionalBeforeRequired() {
    JavaFileObject javaFile = fromSource(
        "@CommandLineArguments",
        "abstract class Arguments {",
        "  @PositionalParameter(position = 0) abstract Optional<String> a();",
        "  @PositionalParameter(position = 1) abstract String b();",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("Invalid position");
  }

  @Test
  void positionalListBeforeOptional() {
    JavaFileObject javaFile = fromSource(
        "@CommandLineArguments",
        "abstract class Arguments {",
        "  @PositionalParameter(position = 0) abstract List<String> a();",
        "  @PositionalParameter(position = 1) abstract Optional<String> b();",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("Invalid position");
  }

  @Test
  void positionalListBeforeRequired() {
    JavaFileObject javaFile = fromSource(
        "@CommandLineArguments",
        "abstract class Arguments {",
        "  @PositionalParameter(position = 0) abstract List<String> a();",
        "  @PositionalParameter(position = 1) abstract String b();",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("Invalid position");
  }

  @Test
  void mayNotDeclareAsOptional() {
    JavaFileObject javaFile = fromSource(
        "@CommandLineArguments",
        "abstract class Arguments {",
        "  @PositionalParameter abstract Optional<Integer> a();",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .compilesWithoutError();
  }
}
