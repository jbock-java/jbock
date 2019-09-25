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
    JavaFileObject javaFile = fromSource(
        "@CommandLineArguments",
        "abstract class Arguments {",
        "  @Parameter(longName = \"x\") abstract String a();",
        "  @Parameter(longName = \"x\") abstract String b();",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("Duplicate long name");
  }

  @Test
  void duplicateShortName() {
    JavaFileObject javaFile = fromSource(
        "@CommandLineArguments",
        "abstract class Arguments {",
        "  @Parameter(shortName = 'x') abstract String a();",
        "  @Parameter(shortName = 'x') abstract String b();",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("Duplicate short name");
  }

  @Test
  void unknownReturnType() {
    JavaFileObject javaFile = fromSource(
        "@CommandLineArguments",
        "abstract class Arguments {",
        "",
        "  @Parameter(shortName = 'x')",
        "  abstract StringBuilder a();",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("Unknown parameter type.");
  }

  @Test
  void declaredException() {
    JavaFileObject javaFile = fromSource(
        "@CommandLineArguments",
        "abstract class Arguments {",
        "",
        "  @Parameter(shortName = 'x')",
        "  abstract String a() throws IllegalArgumentException;",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("may not declare any exceptions");
  }

  @Test
  void classNotAbstract() {
    JavaFileObject javaFile = fromSource(
        "@CommandLineArguments",
        "class Arguments {",
        "",
        "  @Parameter(shortName = 'x')",
        "  String a();",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("The class must be abstract");
  }

  @Test
  void rawList() {
    JavaFileObject javaFile = fromSource(
        "@CommandLineArguments",
        "abstract class Arguments {",
        "",
        "  @Parameter(shortName = 'x')",
        "  abstract List a();",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("Unknown parameter type.");
  }

  @Test
  void rawList2() {
    JavaFileObject javaFile = fromSource(
        "@CommandLineArguments",
        "abstract class Arguments {",
        "",
        "  @Parameter(shortName = 'x')",
        "  abstract List a();",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("Unknown parameter type.");
  }

  @Test
  void rawOptional() {
    JavaFileObject javaFile = fromSource(
        "@CommandLineArguments",
        "abstract class Arguments {",
        "",
        "  @Parameter(shortName = 'x')",
        "  abstract Optional a();",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("Unknown parameter type.");
  }

  @Test
  void rawOptional2() {
    JavaFileObject javaFile = fromSource(
        "@CommandLineArguments",
        "abstract class Arguments {",
        "",
        "  @Parameter(shortName = 'x')",
        "  abstract Optional a();",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("Unknown parameter type.");
  }

  @Test
  void parameterizedSet() {
    JavaFileObject javaFile = fromSource(
        "@CommandLineArguments",
        "abstract class Arguments {",
        "",
        "  @Parameter(shortName = 'x')",
        "  abstract java.util.Set<String> a();",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("Unknown parameter type.");
  }

  @Test
  void integerArray() {
    JavaFileObject javaFile = fromSource(
        "@CommandLineArguments",
        "abstract class Arguments {",
        "",
        "  @Parameter(shortName = 'x')",
        "  abstract int[] a();",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("Unknown parameter type.");
  }

  @Test
  void utilDate() {
    JavaFileObject javaFile = fromSource(
        "@CommandLineArguments",
        "abstract class Arguments {",
        "",
        "  @Parameter(shortName = 'x')",
        "  abstract java.util.Date a();",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("Unknown parameter type.");
  }

  @Test
  void interfaceNotClass() {
    JavaFileObject javaFile = fromSource(
        "@CommandLineArguments",
        "interface Arguments {",
        "  abstract String a();",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("abstract class");
  }

  @Test
  void whitespaceInName() {
    JavaFileObject javaFile = fromSource(
        "@CommandLineArguments",
        "abstract class Arguments {",
        "  @Parameter(longName = \"a \")",
        "  abstract String a();",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("may not contain whitespace");
  }

  @Test
  void noMethods() {
    JavaFileObject javaFile = fromSource(
        "@CommandLineArguments",
        "abstract class Arguments {",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .compilesWithoutError();
  }

  @Test
  void escapeAllowedButNoPositionalArgumentsDefined() {
    JavaFileObject javaFile = fromSource(
        "@CommandLineArguments(allowEscapeSequence = true)",
        "abstract class Arguments {",
        "  @Parameter(shortName = 'a') abstract int a();",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("Define a positional parameter, or disable the escape sequence.");
  }

  @Test
  void oneOptionalIntNotOptional() {
    JavaFileObject javaFile = fromSource(
        "@CommandLineArguments",
        "abstract class Arguments {",
        "",
        "  @Parameter(shortName = 'x')",
        "  abstract OptionalInt b();",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .compilesWithoutError();
  }

  @Test
  void oneOptionalInt() {
    JavaFileObject javaFile = fromSource(
        "@CommandLineArguments",
        "abstract class Arguments {",
        "",
        "  @Parameter(shortName = 'x')",
        "  abstract OptionalInt b();",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .compilesWithoutError();
  }

  @Test
  void simpleFlag() {
    JavaFileObject javaFile = fromSource(
        "@CommandLineArguments",
        "abstract class Arguments {",
        "",
        "  @Parameter(shortName = 'x')",
        "  abstract boolean x();",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .compilesWithoutError();
  }

  @Test
  void simpleInt() {
    JavaFileObject javaFile = fromSource(
        "@CommandLineArguments",
        "abstract class Arguments {",
        "",
        "  @Parameter(shortName = 'x')",
        "  abstract int aRequiredInt();",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .compilesWithoutError();
  }

  @Test
  void extendsNotAllowed() {
    JavaFileObject javaFile = fromSource(
        "abstract class Arguments {",
        "",
        "  @CommandLineArguments",
        "  static abstract class Foo extends Arguments {",
        "    abstract String a();",
        "  }",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("The class may not extend test.Arguments");
  }

  @Test
  void implementsNotAllowed() {
    JavaFileObject javaFile = fromSource(
        "interface Arguments {",
        "",
        "  @CommandLineArguments",
        "  abstract class Foo implements Arguments {",
        "    abstract String a();",
        "  }",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("implement");
  }

  @Test
  void missingCommandLineArgumentsAnnotation() {
    JavaFileObject javaFile = fromSource(
        "abstract class Arguments {",
        "  @Parameter(longName = \"a\") abstract String a();",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("must have the CommandLineArguments annotation");
  }

  @Test
  void noNames() {
    JavaFileObject javaFile = fromSource(
        "@CommandLineArguments",
        "abstract class Arguments {",
        "",
        "  @Parameter",
        "  abstract String a();",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("Define either long name or a short name");
  }

  @Test
  void annotatedMethodNotAbstract() {
    JavaFileObject javaFile = fromSource(
        "@CommandLineArguments",
        "abstract class Arguments {",
        "",
        "  @Parameter(shortName = 'x')",
        "  String a();",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("The method must be abstract.");
  }

  @Test
  void abstractMethodHasParameter() {
    JavaFileObject javaFile = fromSource(
        "@CommandLineArguments",
        "abstract class Arguments {",
        "  @Parameter abstract String a(int b, int c);",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("The method may not have parameters.");
  }

  @Test
  void typeParameter() {
    JavaFileObject javaFile = fromSource(
        "@CommandLineArguments",
        "abstract class Arguments {",
        "  @Parameter abstract <E> String a();",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("The method may not have type parameters.");
  }

  @Test
  void warningNoMethods() {
    JavaFileObject javaFile = fromSource(
        "@CommandLineArguments",
        "abstract class Arguments {",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .compilesWithoutError()
        .withWarningContaining("Define at least one abstract method");
  }

  @Test
  void missingAnnotation() {
    JavaFileObject javaFile = fromSource(
        "@CommandLineArguments",
        "abstract class Arguments {",
        "",
        "  abstract List<String> a();",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("Add Parameter or PositionalParameter annotation");
  }

  @Test
  void positionalFlag() {
    JavaFileObject javaFile = fromSource(
        "@CommandLineArguments",
        "abstract class Arguments {",
        "",
        "  @PositionalParameter",
        "  abstract boolean hello();",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("Unknown parameter type.");
  }

  @Test
  void nearNameCollision() {
    JavaFileObject javaFile = fromSource(
        "@CommandLineArguments",
        "abstract class Arguments {",
        "",
        "  @Parameter(longName = \"fAncy\")",
        "  abstract String fAncy();",

        "  @Parameter(longName = \"f_ancy\")",
        "  abstract String f_ancy();",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .compilesWithoutError();
  }

  @Test
  void doubleAnnotation() {
    JavaFileObject javaFile = fromSource(
        "@CommandLineArguments",
        "abstract class Arguments {",
        "",
        "  @Parameter(shortName = 'x')",
        "  @PositionalParameter",
        "  abstract List<String> a();",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("Remove Parameter or PositionalParameter annotation");
  }

  @Test
  void twoLists() {
    JavaFileObject javaFile = fromSource(
        "@CommandLineArguments",
        "abstract class Arguments {",
        "",
        "  @Parameter(shortName = 'x')",
        "  abstract List<String> a();",
        "",
        "  @PositionalParameter",
        "  abstract List<String> b();",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .compilesWithoutError();
  }

  @Test
  void innerEnum() {
    JavaFileObject javaFile = fromSource(
        "@CommandLineArguments",
        "abstract class Arguments {",
        "",
        "  @Parameter(shortName = 'x')",
        "  abstract Foo foo();",
        "",
        "  enum Foo {",
        "    BAR",
        "   }",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .compilesWithoutError();
  }

  @Test
  void privateEnum() {
    JavaFileObject javaFile = fromSource(
        "@CommandLineArguments",
        "abstract class Arguments {",
        "",
        "  @Parameter(shortName = 'x')",
        "  abstract Foo foo();",
        "",
        "  private enum Foo {",
        "    BAR",
        "   }",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("The enum may not be private.");
  }

  static JavaFileObject fromSource(String... lines) {
    List<String> sourceLines = withImports(lines);
    return forSourceLines("test.Arguments", sourceLines);
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
