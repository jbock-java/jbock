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
        "  @Param(0)",
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
        "  @Param(0)",
        "  abstract Optional<String> a();",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor(true))
        .compilesWithoutError();
  }

  @Test
  void superComplexOptional() {
    JavaFileObject javaFile = fromSource(
        "@SuperCommand",
        "abstract class Arguments {",
        "",
        "  @Param(0)",
        "  abstract Optional<String> a();",
        "",
        "  @Option(\"b\")",
        "  abstract Optional<String> b();",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor(true))
        .compilesWithoutError();
  }

  @Test
  void mixTypeAnnotations() {
    JavaFileObject javaFile = fromSource(
        "@Command",
        "@SuperCommand",
        "abstract class Arguments {",
        "",
        "  @Param(0)",
        "  abstract Optional<String> a();",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("annotate with @Command or @SuperCommand but not both");
  }

  @Test
  void repeatableSuperCommand() {
    JavaFileObject javaFile = fromSource(
        "@SuperCommand",
        "abstract class Arguments {",
        "",
        "  @Param(0)",
        "  abstract List<String> a();",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("when using @SuperCommand, repeatable params are not supported");
  }

  @Test
  void bundleKeyNotUnique() {
    JavaFileObject javaFile = fromSource(
        "@Command",
        "abstract class Arguments {",
        "",
        "  @Param(value = 0, bundleKey = \"x\")",
        "  abstract String a();",
        "",
        "  @Option(value = \"x\", bundleKey = \"x\")",
        "  abstract String b();",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("duplicate bundle key");
  }

  @Test
  void mayNotDeclareAsOptionalInt() {
    JavaFileObject javaFile = fromSource(
        "@Command",
        "abstract class Arguments {",
        "  @Param(0) abstract Optional<Integer> a();",
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
        "  @Param(0) abstract Optional<Integer> b();",
        "  @Param(10) abstract Optional<String> c();",
        "  @Param(100) abstract Optional<Integer> d();",
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
        "  @Param(1) abstract Optional<Integer> b();",
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
        "  @Param(-1) abstract Optional<String> a();",
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
        "  @Param(1) abstract int a();",
        "  @Param(1) abstract int b();",
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
        "  @Param(1) abstract StringBuilder a();",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("define a mapper that implements Function<String, StringBuilder>");
  }

  @Test
  void positionalAllRanks() {
    JavaFileObject javaFile = fromSource(
        "@Command",
        "abstract class Arguments {",
        "  @Param(0) abstract String b();",
        "  @Param(1) abstract Optional<String> c();",
        "  @Param(2) abstract List<String> a();",
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
        "  @Param(0) abstract List<String> a();",
        "  @Param(1) abstract List<String> b();",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("positional parameter A is also repeatable");
  }

  @Test
  void validList() {
    JavaFileObject javaFile = fromSource(
        "@Command",
        "abstract class Arguments {",
        "  @Param(0) abstract List<String> a();",
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
        "  @Param(0) abstract Optional<String> a();",
        "  @Param(1) abstract String b();",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("position must be less than position of optional parameter A");
  }

  @Test
  void positionalListBeforeOptional() {
    JavaFileObject javaFile = fromSource(
        "@Command",
        "abstract class Arguments {",
        "  @Param(0) abstract List<String> a();",
        "  @Param(1) abstract Optional<String> b();",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("position must be less than position of repeatable parameter A");
  }

  @Test
  void positionalListBeforeRequired() {
    JavaFileObject javaFile = fromSource(
        "@Command",
        "abstract class Arguments {",
        "  @Param(1) abstract List<String> a();",
        "  @Param(2) abstract String b();",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("position must be less than position of repeatable parameter A");
  }

  @Test
  void mayNotDeclareAsOptional() {
    JavaFileObject javaFile = fromSource(
        "@Command",
        "abstract class Arguments {",
        "  @Param(0) abstract Optional<Integer> a();",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .compilesWithoutError();
  }
}
