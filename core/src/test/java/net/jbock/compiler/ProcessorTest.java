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
  void emptyLongName() {
    JavaFileObject javaFile = fromSource(
        "@CLI",
        "abstract class Arguments {",
        "  @Option(\"\") abstract String a();",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("The name may not be empty");
  }

  @Test
  void duplicateLongName() {
    JavaFileObject javaFile = fromSource(
        "@CLI",
        "abstract class Arguments {",
        "  @Option(\"x\") abstract String a();",
        "  @Option(\"x\") abstract String b();",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("Duplicate long name");
  }

  @Test
  void duplicateMnemonic() {
    JavaFileObject javaFile = fromSource(
        "@CLI",
        "abstract class Arguments {",
        "  @Option(value = \"x\", mnemonic = 'x') abstract String a();",
        "  @Option(value = \"y\", mnemonic = 'x') abstract String b();",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("Duplicate short name");
  }

  @Test
  void unknownReturnType() {
    JavaFileObject javaFile = fromSource(
        "@CLI",
        "abstract class Arguments {",
        "",
        "  @Option(\"x\")",
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
        "@CLI",
        "abstract class Arguments {",
        "",
        "  @Option(\"x\")",
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
        "@CLI",
        "class Arguments {",
        "",
        "  @Option(\"x\")",
        "  String a();",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile();
  }

  @Test
  void rawList() {
    JavaFileObject javaFile = fromSource(
        "@CLI",
        "abstract class Arguments {",
        "",
        "  @Option(\"x\")",
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
        "@CLI",
        "abstract class Arguments {",
        "",
        "  @Option(\"x\")",
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
        "@CLI",
        "abstract class Arguments {",
        "",
        "  @Option(\"x\")",
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
        "@CLI",
        "abstract class Arguments {",
        "",
        "  @Option(\"x\")",
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
        "@CLI",
        "abstract class Arguments {",
        "",
        "  @Option(\"x\")",
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
        "@CLI",
        "abstract class Arguments {",
        "",
        "  @Option(\"x\")",
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
        "@CLI",
        "abstract class Arguments {",
        "",
        "  @Option(\"x\")",
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
        "@CLI",
        "interface Arguments {",
        "  abstract String a();",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("Use a class, not an interface");
  }

  @Test
  void whitespaceInName() {
    JavaFileObject javaFile = fromSource(
        "@CLI",
        "abstract class Arguments {",
        "  @Option(\"a \")",
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
        "@CLI",
        "abstract class Arguments {",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("Define at least one abstract method");
  }

  @Test
  void oneOptionalIntNotOptional() {
    JavaFileObject javaFile = fromSource(
        "@CLI",
        "abstract class Arguments {",
        "",
        "  @Option(\"x\")",
        "  abstract OptionalInt b();",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .compilesWithoutError();
  }

  @Test
  void oneOptionalInt() {
    JavaFileObject javaFile = fromSource(
        "@CLI",
        "abstract class Arguments {",
        "",
        "  @Option(value = \"x\")",
        "  abstract OptionalInt b();",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .compilesWithoutError();
  }

  @Test
  void simpleFlag() {
    JavaFileObject javaFile = fromSource(
        "@CLI",
        "abstract class Arguments {",
        "",
        "  @Option(value = \"x\")",
        "  abstract boolean x();",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .compilesWithoutError();
  }

  @Test
  void simpleInt() {
    JavaFileObject javaFile = fromSource(
        "@CLI",
        "abstract class Arguments {",
        "",
        "  @Option(value = \"x\")",
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
        "  @CLI",
        "  static abstract class Foo extends Arguments {",
        "    abstract String a();",
        "  }",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("The model class may not implement or extend anything");
  }

  @Test
  void implementsNotAllowed() {
    JavaFileObject javaFile = fromSource(
        "interface Arguments {",
        "",
        "  @CLI",
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
        "  @Option(value = \"a\") abstract String a();",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("The class must have the @CLI annotation");
  }

  @Test
  void annotatedMethodNotAbstract() {
    JavaFileObject javaFile = fromSource(
        "@CLI",
        "abstract class Arguments {",
        "",
        "  @Option(value = \"x\")",
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
        "@CLI",
        "abstract class Arguments {",
        "  @Option(value = \"x\") abstract String a(int b, int c);",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("The method may not have parameters.");
  }

  @Test
  void typeParameter() {
    JavaFileObject javaFile = fromSource(
        "@CLI",
        "abstract class Arguments {",
        "  @Option(value = \"x\") abstract <E> String a();",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("The method may not have type parameters.");
  }

  @Test
  void missingAnnotation() {
    JavaFileObject javaFile = fromSource(
        "@CLI",
        "abstract class Arguments {",
        "",
        "  abstract List<String> a();",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("Annotate this method with either @Option or @Param");
  }

  @Test
  void positionalFlag() {
    JavaFileObject javaFile = fromSource(
        "@CLI",
        "abstract class Arguments {",
        "",
        "  @Param(value = 1)",
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
        "@CLI",
        "abstract class Arguments {",
        "",
        "  @Option(value = \"fAncy\")",
        "  abstract String fAncy();",

        "  @Option(value = \"f_ancy\")",
        "  abstract String f_ancy();",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .compilesWithoutError();
  }

  @Test
  void doubleAnnotation() {
    JavaFileObject javaFile = fromSource(
        "@CLI",
        "abstract class Arguments {",
        "",
        "  @Option(\"x\")",
        "  @Param(1)",
        "  abstract List<String> a();",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("Use either @Option or @Param annotation, but not both");
  }

  @Test
  void twoLists() {
    JavaFileObject javaFile = fromSource(
        "@CLI",
        "abstract class Arguments {",
        "",
        "  @Option(\"x\")",
        "  abstract List<String> a();",
        "",
        "  @Param(1)",
        "  abstract List<String> b();",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .compilesWithoutError();
  }

  @Test
  void innerEnum() {
    JavaFileObject javaFile = fromSource(
        "@CLI",
        "abstract class Arguments {",
        "",
        "  @Option(\"x\")",
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
        "@CLI",
        "abstract class Arguments {",
        "",
        "  @Option(\"x\")",
        "  abstract Foo foo();",
        "",
        "  private enum Foo {",
        "    BAR",
        "   }",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("Unknown parameter type. Try defining a custom mapper or collector.");
  }


  @Test
  void invalidNesting() {
    JavaFileObject javaFile = fromSource(
        "class Bob {",
        "  private static class Foo {",
        "    @CLI",
        "    abstract static class Bar {",
        "    }",
        "  }",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("The class may not not be private");
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
        "import java.util.Collection;",
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
        "import net.jbock.CLI;",
        "import net.jbock.Param;",
        "import net.jbock.Option;",
        "");
    List<String> moreLines = new ArrayList<>(lines.length + header.size());
    moreLines.addAll(header);
    Collections.addAll(moreLines, lines);
    return moreLines;
  }
}
