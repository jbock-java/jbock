package net.jbock.compiler;

import org.junit.jupiter.api.Test;

import javax.tools.JavaFileObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.google.common.truth.Truth.assertAbout;
import static com.google.testing.compile.JavaFileObjects.forSourceLines;
import static com.google.testing.compile.JavaSourcesSubjectFactory.javaSources;
import static java.util.Collections.singletonList;

class ProcessorTest {

  @Test
  void duplicateLongName() {
    List<String> sourceLines = withImports(
        "@CommandLineArguments",
        "abstract class Arguments {",
        "  @Parameter(longName = \"x\") abstract String a();",
        "  @Parameter(longName = \"x\") abstract String b();",
        "}");
    JavaFileObject javaFile = forSourceLines("test.Arguments", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("Duplicate long name");
  }

  @Test
  void duplicateShortName() {
    List<String> sourceLines = withImports(
        "@CommandLineArguments",
        "abstract class Arguments {",
        "  @Parameter(shortName = 'x') abstract String a();",
        "  @Parameter(shortName = 'x') abstract String b();",
        "}");
    JavaFileObject javaFile = forSourceLines("test.Arguments", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("Duplicate short name");
  }

  @Test
  void unknownReturnType() {
    List<String> sourceLines = withImports(
        "@CommandLineArguments",
        "abstract class Arguments {",
        "",
        "  @Parameter(shortName = 'x')",
        "  abstract StringBuilder a();",
        "}");
    JavaFileObject javaFile = forSourceLines("test.Arguments", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("Unknown parameter type. Define a custom mapper.");
  }

  @Test
  void declaredException() {
    List<String> sourceLines = withImports(
        "@CommandLineArguments",
        "abstract class Arguments {",
        "",
        "  @Parameter(shortName = 'x')",
        "  abstract String a() throws IllegalArgumentException;",
        "}");
    JavaFileObject javaFile = forSourceLines("test.Arguments", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("may not declare any exceptions");
  }

  @Test
  void classNotAbstract() {
    List<String> sourceLines = withImports(
        "@CommandLineArguments",
        "class Arguments {",
        "",
        "  @Parameter(shortName = 'x')",
        "  String a();",
        "}");
    JavaFileObject javaFile = forSourceLines("test.Arguments", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("The class must be abstract");
  }

  @Test
  void rawList() {
    List<String> sourceLines = withImports(
        "@CommandLineArguments",
        "abstract class Arguments {",
        "",
        "  @Parameter(shortName = 'x')",
        "  abstract List a();",
        "}");
    JavaFileObject javaFile = forSourceLines("test.Arguments", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("Unknown parameter type. Define a custom mapper.");
  }

  @Test
  void rawList2() {
    List<String> sourceLines = withImports(
        "@CommandLineArguments",
        "abstract class InvalidArguments {",
        "",
        "  @Parameter(shortName = 'x')",
        "  abstract List a();",
        "}");
    JavaFileObject javaFile = forSourceLines("test.InvalidArguments", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("Unknown parameter type. Define a custom mapper.");
  }

  @Test
  void rawOptional() {
    List<String> sourceLines = withImports(
        "@CommandLineArguments",
        "abstract class InvalidArguments {",
        "",
        "  @Parameter(shortName = 'x')",
        "  abstract Optional a();",
        "}");
    JavaFileObject javaFile = forSourceLines("test.InvalidArguments", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("Unknown parameter type. Define a custom mapper.");
  }

  @Test
  void rawOptional2() {
    List<String> sourceLines = withImports(
        "@CommandLineArguments",
        "abstract class InvalidArguments {",
        "",
        "  @Parameter(shortName = 'x')",
        "  abstract Optional a();",
        "}");
    JavaFileObject javaFile = forSourceLines("test.InvalidArguments", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("Unknown parameter type. Define a custom mapper.");
  }

  @Test
  void parameterizedSet() {
    List<String> sourceLines = withImports(
        "@CommandLineArguments",
        "abstract class InvalidArguments {",
        "",
        "  @Parameter(shortName = 'x')",
        "  abstract java.util.Set<String> a();",
        "}");
    JavaFileObject javaFile = forSourceLines("test.InvalidArguments", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("Unknown parameter type. Define a custom mapper.");
  }

  @Test
  void integerArray() {
    List<String> sourceLines = withImports(
        "@CommandLineArguments",
        "abstract class InvalidArguments {",
        "",
        "  @Parameter(shortName = 'x')",
        "  abstract int[] a();",
        "}");
    JavaFileObject javaFile = forSourceLines("test.InvalidArguments", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("Unknown parameter type. Define a custom mapper.");
  }

  @Test
  void utilDate() {
    List<String> sourceLines = withImports(
        "@CommandLineArguments",
        "abstract class InvalidArguments {",
        "",
        "  @Parameter(shortName = 'x')",
        "  abstract java.util.Date a();",
        "}");
    JavaFileObject javaFile = forSourceLines("test.InvalidArguments", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("Unknown parameter type. Define a custom mapper.");
  }

  @Test
  void interfaceNotClass() {
    List<String> sourceLines = withImports(
        "@CommandLineArguments",
        "interface InvalidArguments {",
        "  abstract String a();",
        "}");
    JavaFileObject javaFile = forSourceLines("test.InvalidArguments", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("abstract class");
  }

  @Test
  void whitespaceInName() {
    List<String> sourceLines = withImports(
        "@CommandLineArguments",
        "abstract class InvalidArguments {",
        "  @Parameter(longName = \"a \")",
        "  abstract String a();",
        "}");
    JavaFileObject javaFile = forSourceLines("test.InvalidArguments", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("may not contain whitespace");
  }

  @Test
  void noMethods() {
    List<String> sourceLines = withImports(
        "@CommandLineArguments",
        "abstract class Arguments {",
        "}");
    JavaFileObject javaFile = forSourceLines("test.Arguments", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .compilesWithoutError();
  }

  @Test
  void escapeAllowedButNoPositionalArgumentsDefined() {
    List<String> sourceLines = withImports(
        "@CommandLineArguments(allowEscapeSequence = true)",
        "abstract class InvalidArguments {",
        "  @Parameter(shortName = 'a') abstract int a();",
        "}");
    JavaFileObject javaFile = forSourceLines("test.InvalidArguments", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("Define a positional parameter, or disable the escape sequence.");
  }

  @Test
  void oneOptionalIntNotOptional() {
    List<String> sourceLines = withImports(
        "@CommandLineArguments",
        "abstract class Arguments {",
        "",
        "  @Parameter(shortName = 'x')",
        "  abstract OptionalInt b();",
        "}");
    JavaFileObject javaFile = forSourceLines("test.Arguments", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .compilesWithoutError();
  }

  @Test
  void oneOptionalInt() {
    List<String> sourceLines = withImports(
        "@CommandLineArguments",
        "abstract class Arguments {",
        "",
        "  @Parameter(shortName = 'x')",
        "  abstract OptionalInt b();",
        "}");
    JavaFileObject javaFile = forSourceLines("test.Arguments", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .compilesWithoutError();
  }

  @Test
  void simpleFlag() {
    List<String> sourceLines = withImports(
        "@CommandLineArguments",
        "abstract class Arguments {",
        "",
        "  @Parameter(shortName = 'x')",
        "  abstract boolean x();",
        "}");
    JavaFileObject javaFile = forSourceLines("test.Arguments", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .compilesWithoutError();
  }

  @Test
  void simpleInt() {
    List<String> sourceLines = withImports(
        "@CommandLineArguments",
        "abstract class Arguments {",
        "",
        "  @Parameter(shortName = 'x')",
        "  abstract int aRequiredInt();",
        "}");
    JavaFileObject javaFile = forSourceLines("test.Arguments", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .compilesWithoutError();
  }

  @Test
  void extendsNotAllowed() {
    List<String> sourceLines = withImports(
        "abstract class Outer {",
        "",
        "  @CommandLineArguments",
        "  static abstract class InvalidArguments extends Outer {",
        "    abstract String a();",
        "  }",
        "}");
    JavaFileObject javaFile = forSourceLines("test.Outer", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("The class may not extend test.Outer");
  }

  @Test
  void implementsNotAllowed() {
    List<String> sourceLines = withImports(
        "interface Outer {",
        "",
        "  @CommandLineArguments",
        "  abstract class InvalidArguments implements Outer {",
        "    abstract String a();",
        "  }",
        "}");
    JavaFileObject javaFile = forSourceLines("test.Outer", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("implement");
  }

  @Test
  void missingCommandLineArgumentsAnnotation() {
    List<String> sourceLines = withImports(
        "abstract class InvalidArguments {",
        "  @Parameter(longName = \"a\") abstract String a();",
        "}");
    JavaFileObject javaFile = forSourceLines("test.InvalidArguments", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("must have the CommandLineArguments annotation");
  }

  @Test
  void noNames() {
    List<String> sourceLines = withImports(
        "@CommandLineArguments",
        "abstract class InvalidArguments {",
        "",
        "  @Parameter",
        "  abstract String a();",
        "}");
    JavaFileObject javaFile = forSourceLines("test.InvalidArguments", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("Define either long name or a short name");
  }

  @Test
  void annotatedMethodNotAbstract() {
    List<String> sourceLines = withImports(
        "@CommandLineArguments",
        "abstract class InvalidArguments {",
        "",
        "  @Parameter(shortName = 'x')",
        "  String a();",
        "}");
    JavaFileObject javaFile = forSourceLines("test.InvalidArguments", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("The method must be abstract.");
  }

  @Test
  void abstractMethodHasParameter() {
    List<String> sourceLines = withImports(
        "@CommandLineArguments",
        "abstract class InvalidArguments {",
        "  @Parameter abstract String a(int b, int c);",
        "}");
    JavaFileObject javaFile = forSourceLines("test.InvalidArguments", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("The method may not have parameters.");
  }

  @Test
  void typeParameter() {
    List<String> sourceLines = withImports(
        "@CommandLineArguments",
        "abstract class InvalidArguments {",
        "  @Parameter abstract <E> String a();",
        "}");
    JavaFileObject javaFile = forSourceLines("test.InvalidArguments", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("The method may not have type parameters.");
  }

  @Test
  void warningNoMethods() {
    List<String> sourceLines = withImports(
        "@CommandLineArguments",
        "abstract class InvalidArguments {",
        "}");
    JavaFileObject javaFile = forSourceLines("test.InvalidArguments", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .compilesWithoutError()
        .withWarningContaining("Define at least one abstract method");
  }

  @Test
  void missingAnnotation() {
    List<String> sourceLines = withImports(
        "@CommandLineArguments",
        "abstract class InvalidArguments {",
        "",
        "  abstract List<String> a();",
        "}");
    JavaFileObject javaFile = forSourceLines("test.InvalidArguments", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("Add Parameter or PositionalParameter annotation");
  }

  @Test
  void flagNotDeclared() {
    List<String> sourceLines = withImports(
        "@CommandLineArguments",
        "abstract class Arguments {",
        "",
        "  @Parameter(shortName = 'a')",
        "  abstract boolean hello();",
        "}");
    JavaFileObject javaFile = forSourceLines("test.Arguments", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .compilesWithoutError();
  }

  @Test
  void nearNameCollision() {
    List<String> sourceLines = withImports(
        "@CommandLineArguments",
        "abstract class Arguments {",
        "",
        "  @Parameter(longName = \"fAncy\")",
        "  abstract String fAncy();",

        "  @Parameter(longName = \"f_ancy\")",
        "  abstract String f_ancy();",
        "}");
    JavaFileObject javaFile = forSourceLines("test.ValidArguments", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .compilesWithoutError();
  }

  @Test
  void doubleAnnotation() {
    List<String> sourceLines = withImports(
        "@CommandLineArguments",
        "abstract class InvalidArguments {",
        "",
        "  @Parameter(shortName = 'x')",
        "  @PositionalParameter",
        "  abstract List<String> a();",
        "}");
    JavaFileObject javaFile = forSourceLines("test.InvalidArguments", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("Remove Parameter or PositionalParameter annotation");
  }

  @Test
  void twoLists() {
    List<String> sourceLines = withImports(
        "@CommandLineArguments",
        "abstract class ValidArguments {",
        "",
        "  @Parameter(shortName = 'x')",
        "  abstract List<String> a();",
        "",
        "  @PositionalParameter",
        "  abstract List<String> b();",
        "}");
    JavaFileObject javaFile = forSourceLines("test.ValidArguments", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .compilesWithoutError();
  }

  @Test
  void innerEnum() {
    List<String> sourceLines = withImports(
        "@CommandLineArguments",
        "abstract class ValidArguments {",
        "",
        "  @Parameter(shortName = 'x')",
        "  abstract Foo foo();",
        "",
        "  enum Foo {",
        "    BAR",
        "   }",
        "}");
    JavaFileObject javaFile = forSourceLines("test.ValidArguments", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .compilesWithoutError();
  }

  @Test
  void privateEnum() {
    List<String> sourceLines = withImports(
        "@CommandLineArguments",
        "abstract class InvalidArguments {",
        "",
        "  @Parameter(shortName = 'x')",
        "  abstract Foo foo();",
        "",
        "  private enum Foo {",
        "    BAR",
        "   }",
        "}");
    JavaFileObject javaFile = forSourceLines("test.InvalidArguments", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("The enum may not be private.");
  }

  static List<String> withImports(String... lines) {
    List<String> header = Arrays.asList(
        "package test;",
        "",
        "import java.math.BigInteger;",
        "import java.util.List;",
        "import java.util.Set;",
        "import java.util.Map;",
        "import java.util.AbstractMap;",
        "import java.util.Collections;",
        "import java.util.Optional;",
        "import java.util.OptionalInt;",
        "import java.util.function.Function;",
        "import java.util.function.Supplier;",
        "import java.util.function.BiConsumer;",
        "import java.util.function.BinaryOperator;",
        "import java.util.stream.Collector;",
        "import java.util.stream.Collectors;",
        "import java.time.LocalDate;",
        "",
        "import net.jbock.CommandLineArguments;",
        "import net.jbock.PositionalParameter;",
        "import net.jbock.Parameter;",
        "");
    List<String> moreLines = new ArrayList<>(lines.length + header.size());
    moreLines.addAll(header);
    Collections.addAll(moreLines, lines);
    return moreLines;
  }
}
