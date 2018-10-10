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
        "abstract class InvalidArguments {",
        "  @Parameter(longName = \"x\") abstract String a();",
        "  @Parameter(longName = \"x\") abstract String b();",
        "}");
    JavaFileObject javaFile = forSourceLines("test.InvalidArguments", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("Duplicate long name: x");
  }

  @Test
  void duplicateShortName() {
    List<String> sourceLines = withImports(
        "@CommandLineArguments",
        "abstract class InvalidArguments {",
        "  @Parameter(shortName = 'x') abstract String a();",
        "  @Parameter(shortName = 'x') abstract String b();",
        "}");
    JavaFileObject javaFile = forSourceLines("test.InvalidArguments", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("Duplicate short name: x");
  }

  @Test
  void unknownReturnType() {
    List<String> sourceLines = withImports(
        "@CommandLineArguments",
        "abstract class InvalidArguments {",
        "  @Parameter abstract StringBuilder a();",
        "}");
    JavaFileObject javaFile = forSourceLines("test.InvalidArguments", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("Bad return type");
  }

  @Test
  void declaredException() {
    List<String> sourceLines = withImports(
        "@CommandLineArguments",
        "abstract class InvalidArguments {",
        "  @Parameter abstract String a() throws IllegalArgumentException;",
        "}");
    JavaFileObject javaFile = forSourceLines("test.InvalidArguments", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("may not declare any exceptions");
  }

  @Test
  void classNotAbstract() {
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
  void rawList() {
    List<String> sourceLines = withImports(
        "@CommandLineArguments",
        "abstract class InvalidArguments {",
        "  @Parameter abstract List a();",
        "}");
    JavaFileObject javaFile = forSourceLines("test.InvalidArguments", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("Raw lists are not supported. Use List<X>.");
  }

  @Test
  void rawOptional() {
    List<String> sourceLines = withImports(
        "@CommandLineArguments",
        "abstract class InvalidArguments {",
        "  @Parameter abstract Optional a();",
        "}");
    JavaFileObject javaFile = forSourceLines("test.InvalidArguments", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("Raw optionals are not supported. Use Optional<X>.");
  }

  @Test
  void parameterizedSet() {
    List<String> sourceLines = withImports(
        "@CommandLineArguments",
        "abstract class InvalidArguments {",
        "  @Parameter(repeatable = true) abstract java.util.Set<String> a();",
        "}");
    JavaFileObject javaFile = forSourceLines("test.InvalidArguments", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("Define a custom collector. Alternatively, use List instead.");
  }

  @Test
  void integerArray() {
    List<String> sourceLines = withImports(
        "@CommandLineArguments",
        "abstract class InvalidArguments {",
        "  @Parameter(repeatable = true) abstract int[] a();",
        "}");
    JavaFileObject javaFile = forSourceLines("test.InvalidArguments", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("Use List, or define a custom collector.");
  }

  @Test
  void utilDate() {
    List<String> sourceLines = withImports(
        "@CommandLineArguments",
        "abstract class InvalidArguments {",
        "  @Parameter abstract java.util.Date a();",
        "}");
    JavaFileObject javaFile = forSourceLines("test.InvalidArguments", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("java.util.Date is not supported. Use a type from java.time instead.");
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
        .withErrorContaining("must be an abstract class, not an interface");
  }

  @Test
  void whitespaceInName() {
    List<String> sourceLines = withImports(
        "@CommandLineArguments",
        "abstract class InvalidArguments {",
        "  @Parameter(longName = \"a \") abstract String a();",
        "}");
    JavaFileObject javaFile = forSourceLines("test.InvalidArguments", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("may not contain whitespace");
  }

  @Test
  void positionalBadReturnTypeStringBuilder() {
    List<String> sourceLines = withImports(
        "@CommandLineArguments",
        "abstract class InvalidArguments {",
        "  @PositionalParameter abstract StringBuilder a();",
        "}");
    JavaFileObject javaFile = forSourceLines("test.InvalidArguments", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("Bad return type");
  }

  @Test
  void validPositionalBoolean() {
    List<String> sourceLines = withImports(
        "@CommandLineArguments",
        "abstract class ValidArguments {",
        "  @PositionalParameter abstract Boolean a();",
        "}");
    JavaFileObject javaFile = forSourceLines("test.ValidArguments", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .compilesWithoutError();
  }

  @Test
  void validPositionalboolean() {
    List<String> sourceLines = withImports(
        "@CommandLineArguments",
        "abstract class InvalidArguments {",
        "  @PositionalParameter abstract boolean a();",
        "}");
    JavaFileObject javaFile = forSourceLines("test.InvalidArguments", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .compilesWithoutError();
  }

  @Test
  void positionalListBeforeRequiredPositional() {
    List<String> sourceLines = withImports(
        "@CommandLineArguments",
        "abstract class InvalidArguments {",
        "  @PositionalParameter(repeatable = true) abstract List<String> a();",
        "  @PositionalParameter abstract String b();",
        "}");
    JavaFileObject javaFile = forSourceLines("test.InvalidArguments", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("REQUIRED method b() must come before LIST method a()");
  }

  @Test
  void twoPositionalLists() {
    List<String> sourceLines = withImports(
        "@CommandLineArguments",
        "abstract class InvalidArguments {",
        "  @PositionalParameter(repeatable = true) abstract List<String> a();",
        "  @PositionalParameter(repeatable = true) abstract List<String> b();",
        "}");
    JavaFileObject javaFile = forSourceLines("test.InvalidArguments", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("Only one positional list allowed");
  }

  @Test
  void positionalListBeforeOptionalPositional() {
    List<String> sourceLines = withImports(
        "@CommandLineArguments",
        "abstract class InvalidArguments {",
        "  @PositionalParameter(repeatable = true) abstract List<String> a();",
        "  @PositionalParameter(optional = true) abstract Optional<String> b();",
        "}");
    JavaFileObject javaFile = forSourceLines("test.InvalidArguments", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("OPTIONAL method b() must come before LIST method a()");
  }

  @Test
  void validList() {
    List<String> sourceLines = withImports(
        "@CommandLineArguments",
        "abstract class ValidArguments {",
        "  @PositionalParameter(repeatable = true) abstract List<String> a();",
        "}");
    JavaFileObject javaFile = forSourceLines("test.ValidArguments", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .compilesWithoutError();
  }

  @Test
  void positionalOptionalBeforeRequiredPositional() {
    List<String> sourceLines = withImports(
        "@CommandLineArguments",
        "abstract class InvalidArguments {",
        "  @PositionalParameter(optional = true) abstract Optional<String> a();",
        "  @PositionalParameter abstract String b();",
        "}");
    JavaFileObject javaFile = forSourceLines("test.InvalidArguments", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("REQUIRED method b() must come before OPTIONAL method a()");
  }

  @Test
  void mustDeclareAsOptional() {
    List<String> sourceLines = withImports(
        "@CommandLineArguments",
        "abstract class InvalidArguments {",
        "  @PositionalParameter abstract Optional<Integer> a();",
        "}");
    JavaFileObject javaFile = forSourceLines("test.InvalidArguments", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("Declare this parameter optional.");
  }

  @Test
  void mustDeclareAsOptionalInt() {
    List<String> sourceLines = withImports(
        "@CommandLineArguments",
        "abstract class InvalidArguments {",
        "  @PositionalParameter abstract OptionalInt a();",
        "}");
    JavaFileObject javaFile = forSourceLines("test.InvalidArguments", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("Declare this parameter optional.");
  }

  @Test
  void positionalOptionalsAnyOrder() {
    List<String> sourceLines = withImports(
        "@CommandLineArguments",
        "abstract class InvalidArguments {",
        "  @PositionalParameter(optional = true) abstract Optional<String> a();",
        "  @PositionalParameter(optional = true) abstract OptionalInt b();",
        "  @PositionalParameter(optional = true) abstract Optional<String> c();",
        "  @PositionalParameter(optional = true) abstract OptionalInt d();",
        "}");
    JavaFileObject javaFile = forSourceLines("test.InvalidArguments", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .compilesWithoutError();
  }

  @Test
  void toStringDefined() {
    List<String> sourceLines = withImports(
        "@CommandLineArguments",
        "abstract class ValidArguments {",
        "  @PositionalParameter(optional = true) abstract Optional<String> a();",
        "  @Override public final String toString() {",
        "    return null;",
        "  }",
        "}");
    JavaFileObject javaFile = forSourceLines("test.ValidArguments", sourceLines);
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
        .withErrorContaining("may not extend");
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
        .withErrorContaining("may not implement");
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
        "  @Parameter(longName = \"\") abstract String a();",
        "}");
    JavaFileObject javaFile = forSourceLines("test.InvalidArguments", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("Neither long nor short name defined for method a()");
  }

  @Test
  void annotatedMethodNotAbstract() {
    List<String> sourceLines = withImports(
        "@CommandLineArguments",
        "abstract class InvalidArguments {",
        "  @Parameter(shortName = 'x') String a();",
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
        .withWarningContaining("Skipping");
  }

  @Test
  void missingAnnotation() {
    List<String> sourceLines = withImports(
        "@CommandLineArguments",
        "abstract class InvalidArguments {",
        "  abstract List<String> a();",
        "}");
    JavaFileObject javaFile = forSourceLines("test.InvalidArguments", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("Expecting either Parameter or PositionalParameter annotation");
  }

  @Test
  void doubleAnnotation() {
    List<String> sourceLines = withImports(
        "@CommandLineArguments",
        "abstract class InvalidArguments {",
        "  @PositionalParameter(repeatable = true) @Parameter(repeatable = true) abstract List<String> a();",
        "}");
    JavaFileObject javaFile = forSourceLines("test.InvalidArguments", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("Cannot have both of Parameter and PositionalParameter");
  }

  @Test
  void twoLists() {
    List<String> sourceLines = withImports(
        "@CommandLineArguments",
        "abstract class ValidArguments {",
        "  @Parameter(repeatable = true) abstract List<String> a();",
        "  @PositionalParameter(repeatable = true) abstract List<String> b();",
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
        "  @Parameter abstract Foo foo();",
        "  enum Foo { BAR; }",
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
        "  @Parameter abstract Foo foo();",
        "  private enum Foo { BAR; }",
        "}");
    JavaFileObject javaFile = forSourceLines("test.InvalidArguments", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("Private return type is not allowed");
  }

  @Test
  void identityMapperValid() {
    List<String> sourceLines = withImports(
        "@CommandLineArguments",
        "abstract class ValidArguments {",
        "  @Parameter(mappedBy = Mapper.class) abstract String string();",
        "  static class Mapper<E> implements Supplier<Function<E, E>> {",
        "    public Function<E, E> get() {",
        "      return Function.identity();",
        "    }",
        "  }",
        "}");
    JavaFileObject javaFile = forSourceLines("test.ValidArguments", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .compilesWithoutError();
  }

  @Test
  void mapperValid() {
    List<String> sourceLines = withImports(
        "@CommandLineArguments",
        "abstract class ValidArguments {",
        "  @Parameter(repeatable = true, mappedBy = Mapper.class) abstract List<OptionalInt> numbers();",
        "  static class Mapper implements Supplier<Function<String, OptionalInt>> {",
        "    public Function<String, OptionalInt> get() {",
        "      return s -> OptionalInt.of(1);",
        "    }",
        "  }",
        "}");
    JavaFileObject javaFile = forSourceLines("test.ValidArguments", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .compilesWithoutError();
  }

  @Test
  void mapperValidBytePrimitive() {
    List<String> sourceLines = withImports(
        "@CommandLineArguments",
        "abstract class ValidArguments {",
        "  @Parameter(mappedBy = Mapper.class) abstract byte number();",
        "  static class Mapper implements Supplier<Function<String, Byte>> {",
        "    public Function<String, Byte> get() {",
        "      return s -> 1;",
        "    }",
        "  }",
        "}");
    JavaFileObject javaFile = forSourceLines("test.ValidArguments", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .compilesWithoutError();
  }

  @Test
  void mapperValidByteBoxed() {
    List<String> sourceLines = withImports(
        "@CommandLineArguments",
        "abstract class ValidArguments {",
        "  @Parameter(mappedBy = Mapper.class) abstract Byte number();",
        "  static class Mapper implements Supplier<Function<String, Byte>> {",
        "    public Function<String, Byte> get() {",
        "      return s -> 1;",
        "    }",
        "  }",
        "}");
    JavaFileObject javaFile = forSourceLines("test.ValidArguments", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .compilesWithoutError();
  }

  @Test
  void mapperValidListOfSet() {
    List<String> sourceLines = withImports(
        "@CommandLineArguments",
        "abstract class ValidArguments {",
        "  @Parameter(repeatable = true, mappedBy = Mapper.class) abstract List<Set<Integer>> sets();",
        "  static class Mapper implements Supplier<Function<String, Set<Integer>>> {",
        "    public Function<String, Set<Integer>> get() {",
        "      return s -> Collections.singleton(Integer.valueOf(s));",
        "    }",
        "  }",
        "}");
    JavaFileObject javaFile = forSourceLines("test.ValidArguments", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .compilesWithoutError();
  }

  @Test
  void mapperInvalidPrivateConstructor() {
    List<String> sourceLines = withImports(
        "@CommandLineArguments",
        "abstract class InvalidArguments {",
        "  @Parameter(mappedBy = Mapper.class) abstract Integer number();",
        "  static class Mapper implements Supplier<Function<String, Integer>> {",
        "    private Mapper() {}",
        "    public Function<String, Integer> get() {",
        "      return s -> 1;",
        "    }",
        "  }",
        "}");
    JavaFileObject javaFile = forSourceLines("test.InvalidArguments", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("must have a package visible constructor");
  }

  @Test
  void mapperInvalidNoDefaultConstructor() {
    List<String> sourceLines = withImports(
        "@CommandLineArguments",
        "abstract class InvalidArguments {",
        "  @Parameter(mappedBy = Mapper.class) abstract Integer number();",
        "  static class Mapper implements Supplier<Function<String, Integer>> {",
        "    Mapper(int i) {}",
        "    public Function<String, Integer> get() {",
        "      return s -> 1;",
        "    }",
        "  }",
        "}");
    JavaFileObject javaFile = forSourceLines("test.InvalidArguments", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("must have a default constructor");
  }

  @Test
  void mapperInvalidConstructorException() {
    List<String> sourceLines = withImports(
        "@CommandLineArguments",
        "abstract class InvalidArguments {",
        "  @Parameter(mappedBy = Mapper.class) abstract Integer number();",
        "  static class Mapper implements Supplier<Function<String, Integer>> {",
        "    Mapper() throws IllegalStateException {}",
        "    public Function<String, Integer> get() {",
        "      return s -> 1;",
        "    }",
        "  }",
        "}");
    JavaFileObject javaFile = forSourceLines("test.InvalidArguments", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("may not declare any exceptions");
  }

  @Test
  void mapperInvalidNonstaticInnerClass() {
    List<String> sourceLines = withImports(
        "@CommandLineArguments",
        "abstract class InvalidArguments {",
        "  @Parameter(mappedBy = Mapper.class) abstract Integer number();",
        "  class Mapper implements Supplier<Function<String, Integer>> {",
        "    public Function<String, Integer> get() {",
        "      return s -> 1;",
        "    }",
        "  }",
        "}");
    JavaFileObject javaFile = forSourceLines("test.InvalidArguments", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("class must be static");
  }

  @Test
  void mapperInvalidNotStringFunction() {
    List<String> sourceLines = withImports(
        "@CommandLineArguments",
        "abstract class InvalidArguments {",
        "  @Parameter(mappedBy = Mapper.class) abstract Integer number();",
        "  static class Mapper implements Supplier<Function<Integer, Integer>> {",
        "    public Function<Integer, Integer> get() {",
        "      return s -> s;",
        "    }",
        "  }",
        "}");
    JavaFileObject javaFile = forSourceLines("test.InvalidArguments", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("There is a problem with the mapper class");
  }

  @Test
  void mapperInvalidReturnsString() {
    List<String> sourceLines = withImports(
        "@CommandLineArguments",
        "abstract class InvalidArguments {",
        "  @Parameter(mappedBy = Mapper.class) abstract Integer number();",
        "  static class Mapper implements Supplier<Function<String, String>> {",
        "    public Function<String, String> get() {",
        "      return Function.identity();",
        "    }",
        "  }",
        "}");
    JavaFileObject javaFile = forSourceLines("test.InvalidArguments", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("must implement Supplier<Function<String, java.lang.Integer>>");
  }

  @Test
  void mapperValidTypevars() {
    List<String> sourceLines = withImports(
        "@CommandLineArguments",
        "abstract class ValidArguments {",
        "  @Parameter(mappedBy = Mapper.class) abstract Supplier<String> string();",
        "  static class Mapper implements Supplier<Function<String, Supplier<String>>> {",
        "    public Function<String, Supplier<String>> get() {",
        "      return s -> () -> s;",
        "    }",
        "  }",
        "}");
    JavaFileObject javaFile = forSourceLines("test.ValidArguments", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .compilesWithoutError();
  }

  @Test
  void mapperValidNestedTypevars() {
    List<String> sourceLines = withImports(
        "@CommandLineArguments",
        "abstract class ValidArguments {",
        "  @Parameter(mappedBy = Mapper.class) abstract Supplier<Optional<String>> string();",
        "  static class Mapper implements Supplier<Function<String, Supplier<Optional<String>>>> {",
        "    public Function<String, Supplier<Optional<String>>> get() {",
        "      return s -> () -> Optional.of(s);",
        "    }",
        "  }",
        "}");
    JavaFileObject javaFile = forSourceLines("test.ValidArguments", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .compilesWithoutError();
  }

  @Test
  void mapperValidStringFunction() {
    List<String> sourceLines = withImports(
        "@CommandLineArguments",
        "abstract class InvalidArguments {",
        "  @Parameter(mappedBy = Mapper.class) abstract Integer number();",
        "  static class Mapper implements Supplier<StringFunction<Integer>> {",
        "    public StringFunction<Integer> get() {",
        "      return s -> 1;",
        "    }",
        "  }",
        "  interface StringFunction<R> extends Function<String, R> {}",
        "}");
    JavaFileObject javaFile = forSourceLines("test.InvalidArguments", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .compilesWithoutError();
  }

  @Test
  void mapperValidComplicatedTree() {
    List<String> sourceLines = withImports(
        "@CommandLineArguments",
        "abstract class ValidArguments {",
        "  @Parameter(mappedBy = Mapper.class) abstract Integer number();",
        "  static class Mapper implements ZapperSupplier {",
        "  public Zapper get() {",
        "      return new Zapper();",
        "    }",
        "  }",
        "  interface ZapperSupplier extends Supplier<Zapper> { }",
        "  static class Zapper implements Foo<String>, Xoxo<Integer>  {",
        "    public Integer apply(String s) {",
        "      return 1;",
        "    }",
        "  }",
        "  interface Xi<A, T, B> extends Function<B, A> { }",
        "  interface Zap<T, B, A> extends Xi<A, T, B> { }",
        "  interface Foo<X> extends Zap<X, String, Integer> { }",
        "  interface Bar<E extends Number> extends Function<String, E> { }",
        "  interface Xoxo<X extends Number> extends Bar<X> { }",
        "}");
    JavaFileObject javaFile = forSourceLines("test.ValidArguments", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .compilesWithoutError();
  }

  @Test
  void mapperInvalidComplicatedTree() {
    List<String> sourceLines = withImports(
        "@CommandLineArguments",
        "abstract class ValidArguments {",
        "  @Parameter(mappedBy = Mapper.class) abstract Integer number();",
        "  static class Mapper implements ZapperSupplier {",
        "  public Zapper get() {",
        "      return new Zapper();",
        "    }",
        "  }",
        "  interface ZapperSupplier extends Supplier<Zapper> { }",
        "  static class Zapper implements Foo<String>, Xoxo<Integer>  {",
        "    public Integer apply(String s) {",
        "      return 1;",
        "    }",
        "  }",
        "  interface Xi<A, T, B> extends Function<A, B> { }",
        "  interface Zap<T, B, A> extends Xi<A, T, B> { }",
        "  interface Foo<X> extends Zap<X, String, Integer> { }",
        "  interface Bar<E extends Number> extends Function<E, String> { }",
        "  interface Xoxo<X extends Number> extends Bar<X> { }",
        "}");
    JavaFileObject javaFile = forSourceLines("test.InvalidArguments", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("There is a problem with the mapper class");
  }

  @Test
  void mapperInvalidRawFunction() {
    List<String> sourceLines = withImports(
        "@CommandLineArguments",
        "abstract class InvalidArguments {",
        "  @Parameter(mappedBy = Mapper.class) abstract Integer number();",
        "  static class Mapper implements Supplier<Function> {",
        "    public Function get() {",
        "      return s -> s;",
        "    }",
        "  }",
        "}");
    JavaFileObject javaFile = forSourceLines("test.InvalidArguments", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("There is a problem with the mapper class");
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
