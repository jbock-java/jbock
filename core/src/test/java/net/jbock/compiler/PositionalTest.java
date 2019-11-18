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
        "  @PositionalParameter(value = 1)",
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
        "  @PositionalParameter(value = 1) abstract Optional<Integer> a();",
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
        "  @PositionalParameter(value = -1) abstract Optional<String> a();",
        "  @PositionalParameter(value = 10) abstract Optional<Integer> b();",
        "  @PositionalParameter(value = 100) abstract Optional<String> c();",
        "  @PositionalParameter(value = 1000) abstract Optional<Integer> d();",
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
        "  @PositionalParameter(value = 1) abstract int a();",
        "  @PositionalParameter(value = 1) abstract int b();",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("Define a unique position.");
  }

  @Test
  void positionalConflict2() {
    JavaFileObject javaFile = fromSource(
        "@CommandLineArguments",
        "abstract class Arguments {",
        "  @PositionalParameter(value = 1) abstract String a();",
        "  @PositionalParameter(value = 1) abstract OptionalInt b();",
        "  @PositionalParameter(value = 2) abstract Optional<String> c();",
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
        "  @PositionalParameter(value = 1) abstract StringBuilder a();",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("Unknown parameter type.");
  }

  @Test
  void positionalAllRanks() {
    JavaFileObject javaFile = fromSource(
        "@CommandLineArguments",
        "abstract class Arguments {",
        "  @PositionalParameter(value = 1) abstract String b();",
        "  @PositionalParameter(value = 2) abstract Optional<String> c();",
        "  @PositionalParameter(value = 3) abstract List<String> a();",
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
        "  @PositionalParameter(value = 1) abstract List<String> a();",
        "  @PositionalParameter(value = 2) abstract List<String> b();",
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
        "  @PositionalParameter(value = 1) abstract List<String> a();",
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
        "  @PositionalParameter(value = 0) abstract Optional<String> a();",
        "  @PositionalParameter(value = 1) abstract String b();",
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
        "  @PositionalParameter(value = 0) abstract List<String> a();",
        "  @PositionalParameter(value = 1) abstract Optional<String> b();",
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
        "  @PositionalParameter(value = 0) abstract List<String> a();",
        "  @PositionalParameter(value = 1) abstract String b();",
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
        "  @PositionalParameter(value = 1) abstract Optional<Integer> a();",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .compilesWithoutError();
  }
}
