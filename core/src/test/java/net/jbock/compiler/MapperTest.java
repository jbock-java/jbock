package net.jbock.compiler;

import org.junit.jupiter.api.Test;

import javax.tools.JavaFileObject;
import java.util.List;

import static com.google.common.truth.Truth.assertAbout;
import static com.google.testing.compile.JavaFileObjects.forSourceLines;
import static com.google.testing.compile.JavaSourcesSubjectFactory.javaSources;
import static java.util.Collections.singletonList;
import static net.jbock.compiler.ProcessorTest.withImports;

class MapperTest {

  @Test
  void validStringArray() {
    List<String> sourceLines = withImports(
        "@CommandLineArguments",
        "abstract class ValidArguments {",
        "",
        "  @Parameter(shortName = 'x',",
        "             optional = true,",
        "             mappedBy = ArrayMapper.class)",
        "  abstract Optional<String[]> stringArray();",
        "",
        "  static class ArrayMapper implements Supplier<Function<String, String[]>> {",
        "    public Function<String, String[]> get() {",
        "      return s -> new String[]{s};",
        "    }",
        "  }",
        "}");
    JavaFileObject javaFile = forSourceLines("test.ValidArguments", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .compilesWithoutError();
  }

  @Test
  void invalidReturnTypeNotOptional() {
    List<String> sourceLines = withImports(
        "@CommandLineArguments",
        "abstract class InalidArguments {",
        "",
        "  @Parameter(shortName = 'x',",
        "             optional = true,",
        "             mappedBy = IdMapper.class)",
        "  abstract String plainString();",
        "",
        "  static class IdMapper implements Supplier<Function<String, String>> {",
        "    public Function<String, String> get() {",
        "      return Function.identity();",
        "    }",
        "  }",
        "}");
    JavaFileObject javaFile = forSourceLines("test.InalidArguments", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("Wrap the parameter type in Optional");
  }

  @Test
  void validBooleanList() {
    List<String> sourceLines = withImports(
        "@CommandLineArguments",
        "abstract class InvalidArguments {",
        "",
        "  @PositionalParameter(repeatable = true, mappedBy = BooleanMapper.class)",
        "  abstract List<Boolean> booleanList();",
        "",
        "  static class BooleanMapper implements Supplier<Function<String, Boolean>> {",
        "    @Override",
        "    public Function<String, Boolean> get() {",
        "      return Boolean::valueOf;",
        "    }",
        "  }",
        "}");
    JavaFileObject javaFile = forSourceLines("test.InvalidArguments", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .compilesWithoutError();
  }

  @Test
  void invalidBounds() {
    List<String> sourceLines = withImports(
        "@CommandLineArguments",
        "abstract class InvalidArguments {",
        "",
        "  @PositionalParameter(mappedBy = BoundMapper.class)",
        "  abstract String a();",
        "",
        "  static class BoundMapper<E extends Integer> implements Supplier<Function<E, E>> {",
        "    @Override",
        "    public Function<E, E> get() {",
        "      return Function.identity();",
        "    }",
        "  }",
        "}");
    JavaFileObject javaFile = forSourceLines("test.InvalidArguments", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("mapper");
  }

  @Test
  void validBounds() {
    List<String> sourceLines = withImports(
        "@CommandLineArguments",
        "abstract class InvalidArguments {",
        "",
        "  @PositionalParameter(mappedBy = BoundMapper.class)",
        "  abstract String a();",
        "",
        "  static class BoundMapper<E extends String> implements Supplier<Function<E, E>> {",
        "    @Override",
        "    public Function<E, E> get() {",
        "      return Function.identity();",
        "    }",
        "  }",
        "}");
    JavaFileObject javaFile = forSourceLines("test.InvalidArguments", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .compilesWithoutError();
  }


  @Test
  void mapperInvalidPrivateConstructor() {
    List<String> sourceLines = withImports(
        "@CommandLineArguments",
        "abstract class InvalidArguments {",
        "",
        "  @Parameter(shortName = 'x', mappedBy = Mapper.class)",
        "  abstract Integer number();",
        "",
        "  static class Mapper implements Supplier<Function<String, Integer>> {",
        "",
        "    private Mapper() {}",
        "",
        "    public Function<String, Integer> get() {",
        "      return s -> 1;",
        "    }",
        "  }",
        "}");
    JavaFileObject javaFile = forSourceLines("test.InvalidArguments", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("constructor");
  }

  @Test
  void mapperInvalidNoDefaultConstructor() {
    List<String> sourceLines = withImports(
        "@CommandLineArguments",
        "abstract class InvalidArguments {",
        "",
        "  @Parameter(shortName = 'x', mappedBy = Mapper.class)",
        "  abstract Integer number();",
        "",
        "  static class Mapper implements Supplier<Function<String, Integer>> {",
        "",
        "    Mapper(int i) {}",
        "",
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
        "",
        "  @Parameter(shortName = 'x', mappedBy = Mapper.class)",
        "  abstract Integer number();",
        "",
        "  static class Mapper implements Supplier<Function<String, Integer>> {",
        "",
        "    Mapper() throws IllegalStateException {}",
        "",
        "    public Function<String, Integer> get() {",
        "      return s -> 1;",
        "    }",
        "  }",
        "}");
    JavaFileObject javaFile = forSourceLines("test.InvalidArguments", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("The mapper class must have a default constructor");
  }

  @Test
  void mapperInvalidNonstaticInnerClass() {
    List<String> sourceLines = withImports(
        "@CommandLineArguments",
        "abstract class InvalidArguments {",
        "",
        "  @Parameter(shortName = 'x', mappedBy = Mapper.class)",
        "  abstract Integer number();",
        "",
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
        "",
        "  @Parameter(shortName = 'x', mappedBy = Mapper.class)",
        "  abstract Integer number();",
        "",
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
        "",
        "  @Parameter(shortName = 'x', mappedBy = Mapper.class)",
        "  abstract Integer number();",
        "",
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
        .withErrorContaining("The mapper should return java.lang.Integer but returns String");
  }

  @Test
  void mapperValidTypevars() {
    List<String> sourceLines = withImports(
        "@CommandLineArguments",
        "abstract class ValidArguments {",
        "",
        "  @Parameter(shortName = 'x', mappedBy = Mapper.class)",
        "  abstract Supplier<String> string();",
        "",
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
        "",
        "  @Parameter(shortName = 'x', mappedBy = Mapper.class)",
        "  abstract Supplier<Optional<String>> string();",
        "",
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
        "",
        "  @Parameter(shortName = 'x', mappedBy = Mapper.class)",
        "  abstract Integer number();",
        "",
        "  static class Mapper implements Supplier<StringFunction<Integer>> {",
        "    public StringFunction<Integer> get() {",
        "      return s -> 1;",
        "    }",
        "  }",
        "",
        "  interface StringFunction<R> extends Function<String, R> {}",
        "",
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
        "",
        "  @Parameter(shortName = 'x', mappedBy = Mapper.class)",
        "  abstract Integer number();",
        "",
        "  static class Mapper implements ZapperSupplier {",
        "    public Zapper get() {",
        "      return new Zapper();",
        "    }",
        "  }",
        "",
        "  interface ZapperSupplier extends Supplier<Zapper> { }",
        "  static class Zapper implements Foo<String>, Xoxo<Integer>  {",
        "    public Integer apply(String s) {",
        "      return 1;",
        "    }",
        "  }",
        "",
        "  interface Xi<A, T, B> extends Function<B, A> { }",
        "",
        "  interface Zap<T, B, A> extends Xi<A, T, B> { }",
        "",
        "  interface Foo<X> extends Zap<X, String, Integer> { }",
        "",
        "  interface Bar<E extends Number> extends Function<String, E> { }",
        "",
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
        "",
        "  @Parameter(shortName = 'x', mappedBy = Mapper.class)",
        "  abstract Integer number();",
        "",
        "  static class Mapper implements ZapperSupplier {",
        "    public Zapper get() {",
        "      return new Zapper();",
        "    }",
        "  }",
        "",
        "  interface ZapperSupplier extends Supplier<Zapper> { }",
        "",
        "  static class Zapper implements Foo<String>, Xoxo<Integer>  {",
        "    public Integer apply(String s) {",
        "      return 1;",
        "    }",
        "  }",
        "",
        "  interface Xi<A, T, B> extends Function<A, B> { }",
        "",
        "  interface Zap<T, B, A> extends Xi<A, T, B> { }",
        "",
        "  interface Foo<X> extends Zap<X, String, Integer> { }",
        "",
        "  interface Bar<E extends Number> extends Function<E, String> { }",
        "",
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
        "",
        "  @Parameter(shortName = 'x', mappedBy = Mapper.class)",
        "  abstract Integer number();",
        "",
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
}
