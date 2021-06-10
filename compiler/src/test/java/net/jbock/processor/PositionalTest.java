package net.jbock.processor;

import org.junit.jupiter.api.Test;

import javax.tools.JavaFileObject;

import static com.google.common.truth.Truth.assertAbout;
import static com.google.testing.compile.JavaSourcesSubjectFactory.javaSources;
import static java.util.Collections.singletonList;
import static net.jbock.processor.ProcessorTest.fromSource;

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
        .processedWith(Processor.testInstance())
        .compilesWithoutError();
  }

  @Test
  void superSimpleOptional() {
    JavaFileObject javaFile = fromSource(
        "@Command(superCommand = true, description = \"y\", descriptionKey = \"y\")",
        "abstract class Arguments {",
        "",
        "  @Parameter(index = 0, description = \"x\", descriptionKey = \"x\", paramLabel = \"x\")",
        "  abstract Optional<String> a();",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(Processor.testInstance())
        .compilesWithoutError();
  }

  @Test
  void superComplexOptional() {
    JavaFileObject javaFile = fromSource(
        "@Command(superCommand = true)",
        "abstract class Arguments {",
        "",
        "  @Parameter(index = 0)",
        "  abstract Optional<String> a();",
        "",
        "  @Option(names = \"--b\")",
        "  abstract Optional<String> b();",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(Processor.testInstance())
        .compilesWithoutError();
  }

  @Test
  void commandAndConverterAnnotations() {
    JavaFileObject javaFile = fromSource(
        "@Command",
        "@Converter",
        "abstract class Arguments {",
        "",
        "  @Parameter(index = 0)",
        "  abstract Optional<String> a();",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(Processor.testInstance())
        .failsToCompile()
        .withErrorContaining("annotate with either @Command or @Converter but not both");
  }

  @Test
  void repeatableSuperCommand() {
    JavaFileObject javaFile = fromSource(
        "@Command(superCommand = true)",
        "abstract class Arguments {",
        "",
        "  @Parameters",
        "  abstract List<String> a();",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(Processor.testInstance())
        .failsToCompile()
        .withErrorContaining("positional parameter may not be repeatable" +
            " when the superCommand attribute is set");
  }

  @Test
  void missingParamSuperCommand() {
    JavaFileObject javaFile = fromSource(
        "@Command(superCommand = true)",
        "abstract class Arguments {",
        "",
        "  @Option(names = \"--a\")",
        "  abstract String a();",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(Processor.testInstance())
        .failsToCompile()
        .withErrorContaining("at least one positional parameter must be defined" +
            " when the superCommand attribute is set");
  }

  @Test
  void duplicateDescriptionKeyParam() {
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
        .processedWith(Processor.testInstance())
        .failsToCompile()
        .withErrorContaining("duplicate description key: myKey");
  }

  @Test
  void duplicateDescriptionKeyCommand() {
    JavaFileObject javaFile = fromSource(
        "@Command(descriptionKey = \"myKey\")",
        "abstract class Arguments {",
        "",
        "  @Parameter(index = 0, descriptionKey = \"myKey\")",
        "  abstract String a();",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(Processor.testInstance())
        .failsToCompile()
        .withErrorContaining("duplicate description key: myKey");
  }

  @Test
  void indexGap() {
    JavaFileObject javaFile = fromSource(
        "@Command",
        "abstract class Arguments {",
        "  @Parameter(index = 0) abstract Optional<Integer> b();",
        "  @Parameter(index = 2) abstract Optional<String> c();",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(Processor.testInstance())
        .failsToCompile()
        .withErrorContaining("Position 2 is not available. Suggested position: 1");
  }


  @Test
  void indexMissingZero() {
    JavaFileObject javaFile = fromSource(
        "@Command",
        "abstract class Arguments {",
        "  @Parameter(index = 1) abstract Optional<Integer> b();",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(Processor.testInstance())
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
        .processedWith(Processor.testInstance())
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
        .processedWith(Processor.testInstance())
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
        .processedWith(Processor.testInstance())
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
        .processedWith(Processor.testInstance())
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
        .processedWith(Processor.testInstance())
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
        .processedWith(Processor.testInstance())
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
        .processedWith(Processor.testInstance())
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
        .processedWith(Processor.testInstance())
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
        .processedWith(Processor.testInstance())
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
        .processedWith(Processor.testInstance())
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
        .processedWith(Processor.testInstance())
        .failsToCompile()
        .withErrorContaining("use @Parameters here");
  }
}
