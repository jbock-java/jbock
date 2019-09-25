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
  void validStringArraySupplier() {
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
        "  static class ArrayMapper implements Function<String, String[]> {",
        "    public String[] apply(String s) {",
        "      return new String[]{s};",
        "    }",
        "  }",
        "}");
    JavaFileObject javaFile = forSourceLines("test.ValidArguments", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .compilesWithoutError();
  }

  @Test
  void validMapperWithTypeParameters() {
    List<String> sourceLines = withImports(
        "@CommandLineArguments",
        "abstract class ValidArguments {",
        "",
        "  @Parameter(shortName = 'x',",
        "             mappedBy = IdentityMapper.class)",
        "  abstract String string();",
        "",
        "  static class IdentityMapper<E> implements Function<E, E> {",
        "    public E apply(E e) {",
        "      return e;",
        "    }",
        "  }",
        "}");
    JavaFileObject javaFile = forSourceLines("test.ValidArguments", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .compilesWithoutError();
  }

  @Test
  void invalidMapperTypeParameterWithBounds() {
    List<String> sourceLines = withImports(
        "@CommandLineArguments",
        "abstract class InvalidArguments {",
        "",
        "  @Parameter(shortName = 'x',",
        "             mappedBy = IdentityMapper.class)",
        "  abstract String string();",
        "",
        "  static class IdentityMapper<E extends Integer> implements Function<E, E> {",
        "    public E apply(E e) {",
        "      return e;",
        "    }",
        "  }",
        "}");
    JavaFileObject javaFile = forSourceLines("test.InvalidArguments", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("There is a problem with the mapper class: invalid bounds.");
  }

  @Test
  void validMapperTypeParameterWithBounds() {
    List<String> sourceLines = withImports(
        "@CommandLineArguments",
        "abstract class ValidArguments {",
        "",
        "  @Parameter(shortName = 'x',",
        "             mappedBy = IdentityMapper.class)",
        "  abstract String string();",
        "",
        "  static class IdentityMapper<E extends java.lang.CharSequence> implements Function<E, E> {",
        "    public E apply(E e) {",
        "      return e;",
        "    }",
        "  }",
        "}");
    JavaFileObject javaFile = forSourceLines("test.ValidArguments", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .compilesWithoutError();
  }

  @Test
  void validMapperTypeParameterSupplierWithBounds() {
    List<String> sourceLines = withImports(
        "@CommandLineArguments",
        "abstract class ValidArguments {",
        "",
        "  @Parameter(shortName = 'x',",
        "             mappedBy = IdentityMapper.class)",
        "  abstract String string();",
        "",
        "  static class IdentityMapper<E extends java.lang.CharSequence> implements Supplier<Function<E, E>> {",
        "    public Function<E, E> get() {",
        "      return null;",
        "    }",
        "  }",
        "}");
    JavaFileObject javaFile = forSourceLines("test.ValidArguments", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .compilesWithoutError();
  }

  @Test
  void invalidMapperTypeParameterSupplierWithBounds() {
    List<String> sourceLines = withImports(
        "@CommandLineArguments",
        "abstract class InvalidArguments {",
        "",
        "  @Parameter(shortName = 'x',",
        "             mappedBy = IdentityMapper.class)",
        "  abstract String string();",
        "",
        "  static class IdentityMapper<E extends Integer> implements Supplier<Function<E, E>> {",
        "    public Function<E, E> get() {",
        "      return null;",
        "    }",
        "  }",
        "}");
    JavaFileObject javaFile = forSourceLines("test.InvalidArguments", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("There is a problem with the mapper class: invalid bounds.");
  }


  @Test
  void invalidFlagMapper() {
    List<String> sourceLines = withImports(
        "@CommandLineArguments",
        "abstract class InvalidArguments {",
        "",
        "  @Parameter(shortName = 'x',",
        "             flag = true,",
        "             mappedBy = FlagMapper.class)",
        "  abstract Boolean flag();",
        "",
        "  static class FlagMapper implements Supplier<Function<String, Boolean>> {",
        "    public Function<String, Boolean> get() {",
        "      return s -> !s.isEmpty();",
        "    }",
        "  }",
        "}");
    JavaFileObject javaFile = forSourceLines("test.InvalidArguments", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("flag parameter can't have a mapper");
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
  void invalidBoundsLong() {
    List<String> sourceLines = withImports(
        "@CommandLineArguments",
        "abstract class InvalidArguments {",
        "",
        "  @Parameter(shortName = 'x', mappedBy = Mapper.class)",
        "  abstract Integer number();",
        "",
        "  static class Mapper implements Katz<Long> {",
        "    public Function<String, Long> get() {",
        "      return null;",
        "    }",
        "  }",
        "",
        "  interface Katz<ZK> extends Supplier<Function<String, ZK>> { }",
        "}");
    JavaFileObject javaFile = forSourceLines("test.InvalidArguments", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("There is a problem with the mapper class: The mapper should return Integer but returns Long.");
  }

  @Test
  void invalidBoundsLong2() {
    List<String> sourceLines = withImports(
        "@CommandLineArguments",
        "abstract class InvalidArguments {",
        "",
        "  @Parameter(shortName = 'x', mappedBy = Mapper.class)",
        "  abstract Integer number();",
        "",
        "  static class Mapper implements A<Long> {",
        "    public Function<String, Long> get() {",
        "      return null;",
        "    }",
        "  }",
        "",
        "  interface A<Z> extends B<String, Z> { }",
        "  interface B<VN, WN> extends C<VN, WN> { }",
        "  interface C<PRT, QRT> extends Supplier<Function<PRT, QRT>> { }",
        "}");
    JavaFileObject javaFile = forSourceLines("test.InvalidArguments", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("There is a problem with the mapper class: The mapper should return Integer but returns Long.");
  }

  @Test
  void validBounds() {
    List<String> sourceLines = withImports(
        "@CommandLineArguments",
        "abstract class ValidArguments {",
        "",
        "  @PositionalParameter(mappedBy = BoundMapper.class)",
        "  abstract String a();",
        "",
        "  static class BoundMapper implements Katz<String> {",
        "    @Override",
        "    public Function<String, String> get() {",
        "      return Function.identity();",
        "    }",
        "  }",
        "",
        "  interface Katz<OR> extends Supplier<Function<OR, OR>> { }",
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
        .withErrorContaining("The mapper should return Integer but returns String.");
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
  void mapperValidExtendsFunction() {
    List<String> sourceLines = withImports(
        "@CommandLineArguments",
        "abstract class ValidArguments {",
        "",
        "  @Parameter(shortName = 'x', mappedBy = Mapper.class)",
        "  abstract Integer number();",
        "",
        "  static class Mapper<E> implements Supplier<StringFunction<E, Integer>> {",
        "    public StringFunction<E, Integer> get() {",
        "      return s -> 1;",
        "    }",
        "  }",
        "",
        "  interface StringFunction<V, X> extends Function<V, X> {}",
        "",
        "}");
    JavaFileObject javaFile = forSourceLines("test.ValidArguments", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .compilesWithoutError();
  }

  @Test
  void mapperInvalidStringFunction() {
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
        "  interface StringFunction<R> extends Function<Long, R> {}",
        "",
        "}");
    JavaFileObject javaFile = forSourceLines("test.InvalidArguments", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("There is a problem with the mapper class: The supplied function must take a String argument, but takes Long.");
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
        "  static class Mapper extends ZapperSupplier {",
        "    public Zapper get() {",
        "      return null;",
        "    }",
        "  }",
        "",
        "  static abstract class ZapperSupplier implements Supplier<Function<String, Integer>> { }",
        "",
        "  static class Zapper implements Foo<String>  {",
        "    public Integer apply(String s) {",
        "      return 1;",
        "    }",
        "  }",
        "",
        "  interface Foo<X> extends Zap<X, String, Integer> { }",
        "  interface Zap<T, B, A> extends Xi<A, T, B> { }",
        "  interface Xi<A, T, B> extends Function<B, A> { }",
        "}");
    JavaFileObject javaFile = forSourceLines("test.ValidArguments", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .compilesWithoutError();
  }

  @Test
  void testMapperTypeSudokuInvalid() {
    List<String> sourceLines = withImports(
        "@CommandLineArguments",
        "abstract class ValidArguments {",
        "",
        "  @Parameter(shortName = 'x', mappedBy = Mapper.class)",
        "  abstract List<List<Integer>> number();",
        "",
        "  static class Mapper<E extends List<List<Integer>>> implements FooSupplier<E> { public Foo<E> get() { return null; } }",
        "  interface FooSupplier<K> extends Supplier<Foo<K>> { }",
        "  interface Foo<X> extends Function<String, List<List<X>>> { }",
        "}");
    JavaFileObject javaFile = forSourceLines("test.ValidArguments", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("There is a problem with the mapper class: The mapper should return List<Integer> but returns List<List<X>>");
  }

  @Test
  void testMapperTypeSudokuValid() {
    List<String> sourceLines = withImports(
        "@CommandLineArguments",
        "abstract class ValidArguments {",
        "",
        "  @Parameter(shortName = 'x', mappedBy = Mapper.class)",
        "  abstract List<List<Integer>> number();",
        "",
        "  static class Mapper<E extends Integer> implements FooSupplier<E> { public Foo<E> get() { return null; } }",
        "  interface FooSupplier<K> extends Supplier<Foo<K>> { }",
        "  interface Foo<X> extends Function<String, List<List<X>>> { }",
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
        "abstract class InvalidArguments {",
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
        "  static class Zapper implements Foo<String> {",
        "    public String apply(Integer s) {",
        "      return null;",
        "    }",
        "  }",
        "",
        "  interface Xi<A, T, B> extends Function<A, B> { }",
        "  interface Zap<T, B, A> extends Xi<A, T, B> { }",
        "  interface Foo<X> extends Zap<X, String, Integer> { }",
        "}");
    JavaFileObject javaFile = forSourceLines("test.InvalidArguments", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("There is a problem with the mapper class: The supplied function must take a String argument, but takes Integer.");
  }

  @Test
  void mapperInvalidNotFunction() {
    List<String> sourceLines = withImports(
        "@CommandLineArguments",
        "abstract class InvalidArguments {",
        "",
        "  @Parameter(shortName = 'x', mappedBy = Mapper.class)",
        "  abstract Integer number();",
        "",
        "  static class Mapper implements ZapperSupplier {",
        "    public String get() {",
        "      return Integer.toString(1);",
        "    }",
        "  }",
        "",
        "  interface ZapperSupplier extends Supplier<String> { }",
        "}");
    JavaFileObject javaFile = forSourceLines("test.InvalidArguments", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("There is a problem with the mapper class: not a Function or Supplier<Function>.");
  }

  @Test
  void mapperInvalidFunctionReturnType() {
    List<String> sourceLines = withImports(
        "@CommandLineArguments",
        "abstract class InvalidArguments {",
        "",
        "  @Parameter(shortName = 'x', mappedBy = Mapper.class)",
        "  abstract Integer number();",
        "",
        "  static class Mapper implements ZapperSupplier {",
        "    public Function<String, Long> get() {",
        "      return s -> 1L;",
        "    }",
        "  }",
        "",
        "  interface ZapperSupplier extends Supplier<Function<String, Long>> { }",
        "}");
    JavaFileObject javaFile = forSourceLines("test.InvalidArguments", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("There is a problem with the mapper class: The mapper should return Integer but returns Long.");
  }

  @Test
  void mapperInvalidBounds() {
    List<String> sourceLines = withImports(
        "@CommandLineArguments",
        "abstract class InvalidArguments {",
        "",
        "  @Parameter(shortName = 'x', mappedBy = Mapper.class)",
        "  abstract Integer number();",
        "",
        "  static class Mapper implements ZapperSupplier<java.util.Date> {",
        "    public Function<String, java.util.Date> get() {",
        "      return null;",
        "    }",
        "  }",
        "",
        "  interface ZapperSupplier<E extends java.util.Date> extends Supplier<Function<String, E>> { }",
        "}");
    JavaFileObject javaFile = forSourceLines("test.InvalidArguments", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("There is a problem with the mapper class: The mapper should return Integer but returns java.util.Date");
  }

  @Test
  void mapperValidBounds() {
    List<String> sourceLines = withImports(
        "@CommandLineArguments",
        "abstract class ValidArguments {",
        "",
        "  @Parameter(shortName = 'x', mappedBy = Mapper.class)",
        "  abstract Integer number();",
        "",
        "  static class Mapper implements A<Integer, String> {",
        "    public Function<String, Integer> get() {",
        "      return null;",
        "    }",
        "  }",
        "",
        "  interface A<R, X> extends B<X, R> { }",
        "  interface B<X, R> extends C<X, R> { }",
        "  interface C<X, R> extends Supplier<Function<X, R>> { }",
        "}");
    JavaFileObject javaFile = forSourceLines("test.InvalidArguments", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .compilesWithoutError();
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
        .withErrorContaining("There is a problem with the mapper class: the function type must be parameterized");
  }

  @Test
  void mapperValid() {
    List<String> sourceLines = withImports(
        "@CommandLineArguments",
        "abstract class ValidArguments {",
        "",
        "  @Parameter(shortName = 'x', repeatable = true, mappedBy = Mapper.class)",
        "  abstract List<OptionalInt> numbers();",
        "",
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
  void mapperValidByte() {
    List<String> sourceLines = withImports(
        "@CommandLineArguments",
        "abstract class ValidArguments {",
        "",
        "  @Parameter(shortName = 'x', mappedBy = Mapper.class)",
        "  abstract Byte number();",
        "",
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
  void mapperValidBytePrimitive() {
    List<String> sourceLines = withImports(
        "@CommandLineArguments",
        "abstract class ValidArguments {",
        "",
        "  @Parameter(shortName = 'x', mappedBy = Mapper.class)",
        "  abstract byte number();",
        "",
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
  void mapperValidOptionalInteger() {
    List<String> sourceLines = withImports(
        "@CommandLineArguments",
        "abstract class ValidArguments {",
        "",
        "  @Parameter(shortName = 'x', mappedBy = Mapper.class, optional = true)",
        "  abstract Optional<Integer> number();",
        "",
        "  static class Mapper implements Supplier<Function<String, Integer>> {",
        "    public Function<String, Integer> get() {",
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
  void mapperValidOptionalStringTypevar() {
    List<String> sourceLines = withImports(
        "@CommandLineArguments",
        "abstract class ValidArguments {",
        "",
        "  @Parameter(shortName = 'x', mappedBy = Mapper.class, optional = true)",
        "  abstract Optional<String> number();",
        "",
        "  static class Mapper<E> implements Supplier<Function<E, E>> {",
        "    public Function<E, E> get() {",
        "      return null;",
        "    }",
        "  }",
        "}");
    JavaFileObject javaFile = forSourceLines("test.ValidArguments", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .compilesWithoutError();
  }

  @Test
  void mapperValidStringOptionalStringTypevar() {
    List<String> sourceLines = withImports(
        "@CommandLineArguments",
        "abstract class ValidArguments {",
        "",
        "  @Parameter(shortName = 'x', mappedBy = Mapper.class, optional = true)",
        "  abstract Optional<String> number();",
        "",
        "  static class Mapper<E> implements Supplier<Function<E, Optional<E>>> {",
        "    public Function<E, Optional<E>> get() {",
        "      return null;",
        "    }",
        "  }",
        "}");
    JavaFileObject javaFile = forSourceLines("test.ValidArguments", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .compilesWithoutError();
  }

  @Test
  void mapperValidStringListTypevar() {
    List<String> sourceLines = withImports(
        "@CommandLineArguments",
        "abstract class ValidArguments {",
        "",
        "  @Parameter(shortName = 'x', mappedBy = Mapper.class)",
        "  abstract List<String> number();",
        "",
        "  static class Mapper<E> implements Supplier<Function<E, List<E>>> {",
        "    public Function<E, List<E>> get() {",
        "      return null;",
        "    }",
        "  }",
        "}");
    JavaFileObject javaFile = forSourceLines("test.ValidArguments", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .compilesWithoutError();
  }

  @Test
  void implicitMapperOptionalInt() {
    List<String> sourceLines = withImports(
        "@CommandLineArguments",
        "abstract class InvalidArguments {",
        "",
        "  @Parameter(shortName = 'x', mappedBy = Mapper.class)",
        "  abstract OptionalInt b();",
        "",
        "  static class Mapper implements Supplier<Function<String, Integer>> {",
        "    public Function<String, Integer> get() {",
        "      return s -> 1;",
        "    }",
        "  }",
        "}");
    JavaFileObject javaFile = forSourceLines("test.InvalidArguments", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .compilesWithoutError();
  }

  @Test
  void mapperOptionalInt() {
    List<String> sourceLines = withImports(
        "@CommandLineArguments",
        "abstract class ValidArguments {",
        "",
        "  @Parameter(shortName = 'x', mappedBy = Mapper.class)",
        "  abstract OptionalInt b();",
        "",
        "  static class Mapper implements Supplier<Function<String, OptionalInt>> {",
        "    public Function<String, OptionalInt> get() {",
        "      return null;",
        "    }",
        "  }",
        "}");
    JavaFileObject javaFile = forSourceLines("test.ValidArguments", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .compilesWithoutError();
  }

  @Test
  void mapperOptionalInteger() {
    List<String> sourceLines = withImports(
        "@CommandLineArguments",
        "abstract class ValidArguments {",
        "",
        "  @Parameter(shortName = 'x', mappedBy = Mapper.class)",
        "  abstract Optional<Integer> b();",
        "",
        "  static class Mapper implements Supplier<Function<String, Optional<Integer>>> {",
        "    public Function<String, Optional<Integer>> get() {",
        "      return null;",
        "    }",
        "  }",
        "}");
    JavaFileObject javaFile = forSourceLines("test.ValidArguments", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .compilesWithoutError();
  }

  @Test
  void oneOptionalInt() {
    List<String> sourceLines = withImports(
        "@CommandLineArguments",
        "abstract class ValidArguments {",
        "",
        "  @Parameter(shortName = 'x', mappedBy = Mapper.class, optional = true)",
        "  abstract OptionalInt b();",
        "",
        "  static class Mapper implements Supplier<Function<String, Integer>> {",
        "    public Function<String, Integer> get() {",
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
        "",
        "  @Parameter(shortName = 'x', repeatable = true, mappedBy = Mapper.class)",
        "  abstract List<Set<Integer>> sets();",
        "",
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
  void mapperHasTypeargsImpossibleFromString() {
    List<String> sourceLines = withImports(
        "@CommandLineArguments",
        "abstract class InvalidArguments {",
        "",
        "  @Parameter(shortName = 'x',",
        "             repeatable = true,",
        "             mappedBy = Identity.class)",
        "  abstract List<Integer> ints();",
        "",
        "  static class Identity<E> implements Supplier<Function<E, E>> {",
        "    public Function<E, E> get() {",
        "      return null;",
        "    }",
        "  }",
        "}");
    JavaFileObject javaFile = forSourceLines("test.InvalidArguments", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("There is a problem with the mapper class: could not infer type parameters.");
  }
}
