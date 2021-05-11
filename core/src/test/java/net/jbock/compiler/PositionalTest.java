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
        "@Command",
        "abstract class Arguments {",
        "",
        "  @Parameter(index = 0)",
        "  abstract Optional<String> a();",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .compilesWithoutError();
  }

  @Test
  void superSimpleOptional() {
    JavaFileObject javaFile = fromSource(
        "@SuperCommand",
        "abstract class Arguments {",
        "",
        "  @Parameter(index = 0)",
        "  abstract Optional<String> a();",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .compilesWithoutError();
  }

  @Test
  void superComplexOptional() {
    JavaFileObject javaFile = fromSource(
        "@SuperCommand",
        "abstract class Arguments {",
        "",
        "  @Parameter(index = 0)",
        "  abstract Optional<String> a();",
        "",
        "  @Option(names = \"--b\")",
        "  abstract Optional<String> b();",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .compilesWithoutError();
  }

  @Test
  void duplicateCommandAnnotations() {
    JavaFileObject javaFile = fromSource(
        "@Command",
        "@SuperCommand",
        "abstract class Arguments {",
        "",
        "  @Parameter(index = 0)",
        "  abstract Optional<String> a();",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("annotate with either @Command or @SuperCommand but not both");
  }

  @Test
  void repeatableSuperCommand() {
    JavaFileObject javaFile = fromSource(
        "@SuperCommand",
        "abstract class Arguments {",
        "",
        "  @Parameters",
        "  abstract List<String> a();",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("@Parameters cannot be used in a @SuperCommand");
  }

  @Test
  void missingParamSuperCommand() {
    JavaFileObject javaFile = fromSource(
        "@SuperCommand",
        "abstract class Arguments {",
        "",
        "  @Option(names = \"--a\")",
        "  abstract String a();",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("in a @SuperCommand, at least one @Parameter must be defined");
  }

  @Test
  void descriptionKeyNotUnique() {
    JavaFileObject javaFile = fromSource(
        "@Command",
        "abstract class Arguments {",
        "",
        "  @Parameter(index = 0, descriptionKey = \"myKey\")",
        "  abstract String a();",
        "",
        "  @Option(names = \"--x\", descriptionKey = \"myKey\")",
        "  abstract String b();",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("duplicate description key: myKey");
  }

  @Test
  void mayNotDeclareAsOptionalInt() {
    JavaFileObject javaFile = fromSource(
        "@Command",
        "abstract class Arguments {",
        "  @Parameter(index = 0) abstract Optional<Integer> a();",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .compilesWithoutError();
  }

  @Test
  void positionalOptionalsGaps() {
    JavaFileObject javaFile = fromSource(
        "@Command",
        "abstract class Arguments {",
        "  @Parameter(index = 0) abstract Optional<Integer> b();",
        "  @Parameter(index = 10) abstract Optional<String> c();",
        "  @Parameter(index = 100) abstract Optional<Integer> d();",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("Position 10 is not available. Suggested position: 1");
  }


  @Test
  void positionalOptionalsOne() {
    JavaFileObject javaFile = fromSource(
        "@Command",
        "abstract class Arguments {",
        "  @Parameter(index = 1) abstract Optional<Integer> b();",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("Position 1 is not available. Suggested position: 0");
  }


  @Test
  void positionalOptionalsNegative() {
    JavaFileObject javaFile = fromSource(
        "@Command",
        "abstract class Arguments {",
        "  @Parameter(index = -1) abstract Optional<String> a();",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("negative positions are not allowed");
  }

  @Test
  void positionalConflict() {
    JavaFileObject javaFile = fromSource(
        "@Command",
        "abstract class Arguments {",
        "  @Parameter(index = 1) abstract int a();",
        "  @Parameter(index = 1) abstract int b();",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("duplicate position");
  }


  @Test
  void positionalBadReturnTypeStringBuilder() {
    JavaFileObject javaFile = fromSource(
        "@Command",
        "abstract class Arguments {",
        "  @Parameter(index = 1) abstract StringBuilder a();",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("define a converter that implements Function<String, StringBuilder>");
  }

  @Test
  void positionalAllRanks() {
    JavaFileObject javaFile = fromSource(
        "@Command",
        "abstract class Arguments {",
        "  @Parameter(index = 0) abstract String b();",
        "  @Parameter(index = 1) abstract Optional<String> c();",
        "  @Parameters abstract List<String> a();",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .compilesWithoutError();
  }

  @Test
  void twoPositionalLists() {
    JavaFileObject javaFile = fromSource(
        "@Command",
        "abstract class Arguments {",
        "  @Parameters abstract List<String> a();",
        "  @Parameters abstract List<String> b();",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("duplicate @Parameters annotation");
  }

  @Test
  void validList() {
    JavaFileObject javaFile = fromSource(
        "@Command",
        "abstract class Arguments {",
        "  @Parameters abstract List<String> a();",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .compilesWithoutError();
  }

  @Test
  void positionalOptionalBeforeRequired() {
    JavaFileObject javaFile = fromSource(
        "@Command",
        "abstract class Arguments {",
        "  @Parameter(index = 0) abstract Optional<String> a();",
        "  @Parameter(index = 1) abstract String b();",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("position must be less than position of optional parameter A");
  }

  @Test
  void mayNotDeclareAsOptional() {
    JavaFileObject javaFile = fromSource(
        "@Command",
        "abstract class Arguments {",
        "  @Parameter(index = 0) abstract Optional<Integer> a();",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .compilesWithoutError();
  }

  @Test
  void parametersInvalidNotList() {
    JavaFileObject javaFile = fromSource(
        "@Command",
        "abstract class Arguments {",
        "",
        "  @Parameters",
        "  abstract Integer something();",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("use @Parameter here");
  }

  @Test
  void parametersInvalidNotListOptional() {
    JavaFileObject javaFile = fromSource(
        "@Command",
        "abstract class Arguments {",
        "",
        "  @Parameters",
        "  abstract Optional<Integer> something();",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("use @Parameter here");
  }

  @Test
  void parameterInvalidList() {
    JavaFileObject javaFile = fromSource(
        "@Command",
        "abstract class Arguments {",
        "",
        "  @Parameter(index = 0)",
        "  abstract List<Integer> something();",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("use @Parameters here");
  }
}
