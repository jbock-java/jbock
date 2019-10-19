package net.jbock.compiler;

import org.junit.jupiter.api.Test;

import javax.tools.JavaFileObject;

import static com.google.common.truth.Truth.assertAbout;
import static com.google.testing.compile.JavaSourcesSubjectFactory.javaSources;
import static java.util.Collections.singletonList;
import static net.jbock.compiler.ProcessorTest.fromSource;

class MapperTest {

  @Test
  void validArrayMapperSupplier() {
    JavaFileObject javaFile = fromSource(
        "@CommandLineArguments",
        "abstract class Arguments {",
        "",
        "  @Parameter(shortName = 'x',",
        "             mappedBy = ArrayMapper.class)",
        "  abstract Optional<int[]> foo();",
        "",
        "  static class ArrayMapper implements Supplier<Function<String, int[]>> {",
        "    public Function<String, int[]> get() {",
        "      return null;",
        "    }",
        "  }",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .compilesWithoutError();
  }

  @Test
  void validArrayMapper() {
    JavaFileObject javaFile = fromSource(
        "@CommandLineArguments",
        "abstract class Arguments {",
        "",
        "  @Parameter(shortName = 'x',",
        "             mappedBy = ArrayMapper.class)",
        "  abstract Optional<int[]> foo();",
        "",
        "  static class ArrayMapper implements Function<String, int[]> {",
        "    public int[] apply(String s) {",
        "      return null;",
        "    }",
        "  }",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .compilesWithoutError();
  }

  @Test
  void validMapperWithTypeParameters() {
    JavaFileObject javaFile = fromSource(
        "@CommandLineArguments",
        "abstract class Arguments {",
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
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .compilesWithoutError();
  }

  @Test
  void invalidMapperTypeParameterWithBounds() {
    JavaFileObject javaFile = fromSource(
        "@CommandLineArguments",
        "abstract class Arguments {",
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
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("There is a problem with the mapper class: invalid bounds.");
  }

  @Test
  void validMapperTypeParameterWithBounds() {
    JavaFileObject javaFile = fromSource(
        "@CommandLineArguments",
        "abstract class Arguments {",
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
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .compilesWithoutError();
  }

  @Test
  void validMapperTypeParameterSupplierWithBounds() {
    JavaFileObject javaFile = fromSource(
        "@CommandLineArguments",
        "abstract class Arguments {",
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
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .compilesWithoutError();
  }

  @Test
  void invalidMapperTypeParameterSupplierWithBounds() {
    JavaFileObject javaFile = fromSource(
        "@CommandLineArguments",
        "abstract class Arguments {",
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
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("There is a problem with the mapper class: invalid bounds.");
  }


  @Test
  void invalidFlagMapper() {
    JavaFileObject javaFile = fromSource(
        "@CommandLineArguments",
        "abstract class Arguments {",
        "",
        "  @Parameter(shortName = 'x',",
        "             mappedBy = FlagMapper.class)",
        "  abstract Boolean flag();",
        "",
        "  static class FlagMapper implements Supplier<Function<String, Boolean>> {",
        "    public Function<String, Boolean> get() {",
        "      return s -> !s.isEmpty();",
        "    }",
        "  }",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .compilesWithoutError();
  }

  @Test
  void validBooleanList() {
    JavaFileObject javaFile = fromSource(
        "@CommandLineArguments",
        "abstract class Arguments {",
        "",
        "  @PositionalParameter(mappedBy = BooleanMapper.class)",
        "  abstract List<Boolean> booleanList();",
        "",
        "  static class BooleanMapper implements Supplier<Function<String, Boolean>> {",
        "    @Override",
        "    public Function<String, Boolean> get() {",
        "      return Boolean::valueOf;",
        "    }",
        "  }",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .compilesWithoutError();
  }

  @Test
  void invalidBounds() {
    JavaFileObject javaFile = fromSource(
        "@CommandLineArguments",
        "abstract class Arguments {",
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
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("mapper");
  }

  @Test
  void invalidBoundsLong() {
    JavaFileObject javaFile = fromSource(
        "@CommandLineArguments",
        "abstract class Arguments {",
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
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("There is a problem with the mapper class: The mapper should return Integer but returns Long.");
  }

  @Test
  void invalidBoundsLong2() {
    JavaFileObject javaFile = fromSource(
        "@CommandLineArguments",
        "abstract class Arguments {",
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
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("There is a problem with the mapper class: The mapper should return Integer but returns Long.");
  }

  @Test
  void validBounds() {
    JavaFileObject javaFile = fromSource(
        "@CommandLineArguments",
        "abstract class Arguments {",
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
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .compilesWithoutError();
  }

  @Test
  void mapperInvalidPrivateConstructor() {
    JavaFileObject javaFile = fromSource(
        "@CommandLineArguments",
        "abstract class Arguments {",
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
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("constructor");
  }

  @Test
  void mapperInvalidNoDefaultConstructor() {
    JavaFileObject javaFile = fromSource(
        "@CommandLineArguments",
        "abstract class Arguments {",
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
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("must have a default constructor");
  }

  @Test
  void mapperInvalidConstructorException() {
    JavaFileObject javaFile = fromSource(
        "@CommandLineArguments",
        "abstract class Arguments {",
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
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("The mapper class must have a default constructor");
  }

  @Test
  void mapperInvalidNonstaticInnerClass() {
    JavaFileObject javaFile = fromSource(
        "@CommandLineArguments",
        "abstract class Arguments {",
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
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("class must be static");
  }

  @Test
  void mapperInvalidNotStringFunction() {
    JavaFileObject javaFile = fromSource(
        "@CommandLineArguments",
        "abstract class Arguments {",
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
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("There is a problem with the mapper class");
  }

  @Test
  void mapperInvalidReturnsString() {
    JavaFileObject javaFile = fromSource(
        "@CommandLineArguments",
        "abstract class Arguments {",
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
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("The mapper should return Integer but returns String.");
  }

  @Test
  void mapperValidTypevars() {
    JavaFileObject javaFile = fromSource(
        "@CommandLineArguments",
        "abstract class Arguments {",
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
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .compilesWithoutError();
  }

  @Test
  void mapperValidNestedTypevars() {
    JavaFileObject javaFile = fromSource(
        "@CommandLineArguments",
        "abstract class Arguments {",
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

    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .compilesWithoutError();
  }

  @Test
  void mapperValidExtendsFunction() {
    JavaFileObject javaFile = fromSource(
        "@CommandLineArguments",
        "abstract class Arguments {",
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
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .compilesWithoutError();
  }

  @Test
  void mapperInvalidStringFunction() {
    JavaFileObject javaFile = fromSource(
        "@CommandLineArguments",
        "abstract class Arguments {",
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
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("There is a problem with the mapper class: The supplied function must take a String argument, but takes Long.");
  }

  @Test
  void mapperValidComplicatedTree() {
    JavaFileObject javaFile = fromSource(
        "@CommandLineArguments",
        "abstract class Arguments {",
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
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .compilesWithoutError();
  }

  @Test
  void testMapperTypeSudokuInvalid() {
    JavaFileObject javaFile = fromSource(
        "@CommandLineArguments",
        "abstract class Arguments {",
        "",
        "  @Parameter(shortName = 'x', mappedBy = Mapper.class)",
        "  abstract List<List<Integer>> number();",
        "",
        "  static class Mapper<E extends List<List<Integer>>> implements FooSupplier<E> { public Foo<E> get() { return null; } }",
        "  interface FooSupplier<K> extends Supplier<Foo<K>> { }",
        "  interface Foo<X> extends Function<String, List<List<X>>> { }",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("There is a problem with the mapper class");
  }

  @Test
  void testMapperTypeSudokuValid() {
    JavaFileObject javaFile = fromSource(
        "@CommandLineArguments",
        "abstract class Arguments {",
        "",
        "  @Parameter(shortName = 'x', mappedBy = Mapper.class)",
        "  abstract List<List<Integer>> number();",
        "",
        "  static class Mapper<E extends Integer> implements FooSupplier<E> { public Foo<E> get() { return null; } }",
        "  interface FooSupplier<K> extends Supplier<Foo<K>> { }",
        "  interface Foo<X> extends Function<String, List<List<X>>> { }",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .compilesWithoutError();
  }

  @Test
  void testLongSudokuValid() {
    JavaFileObject javaFile = fromSource(
        "@CommandLineArguments",
        "abstract class Arguments {",
        "",
        "  @Parameter(shortName = 'x', mappedBy = Mapper.class)",
        "  abstract List<List<List<List<List<List<List<Set<Set<Set<Set<Set<Set<Collection<Integer>>>>>>>>>>>>>> numbers();",
        "",
        "  static class Mapper<M extends Integer> implements Plop1<Collection<M>> {",
        "    public Foo1<Set<Set<Set<Set<Set<Set<Collection<M>>>>>>>> get() { return null; }",
        "  }",
        "  interface Plop1<AA> extends Plop2<Set<AA>> { }",
        "  interface Plop2<BB> extends Plop3<Set<BB>> { }",
        "  interface Plop3<CC> extends Plop4<Set<CC>> { }",
        "  interface Plop4<DD> extends Plop5<Set<DD>> { }",
        "  interface Plop5<EE> extends FooSupplier<Set<EE>> { }",
        "  interface FooSupplier<K> extends Supplier<Foo1<Set<K>>> { }",
        "  interface Foo1<A> extends Foo2<List<A>> { }",
        "  interface Foo2<B> extends Foo3<List<B>> { }",
        "  interface Foo3<C> extends Foo4<List<C>> { }",
        "  interface Foo4<D> extends Foo5<List<D>> { }",
        "  interface Foo5<E> extends Foo6<List<E>> { }",
        "  interface Foo6<F> extends Foo7<List<F>> { }",
        "  interface Foo7<G> extends Foo8<List<G>> { }",
        "  interface Foo8<H> extends Function<String, H> { }",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .compilesWithoutError();
  }

  @Test
  void testListSudoku() {
    JavaFileObject javaFile = fromSource(
        "@CommandLineArguments",
        "abstract class Arguments {",
        "",
        "  @Parameter(shortName = 'x', mappedBy = Mapper.class)",
        "  abstract List<Integer> number();",
        "",
        "  static class Mapper<AA1, AA2> implements A<AA1, AA2> {",
        "    public Function<AA1, List<AA2>> get() { return null; }",
        "  }",
        "  interface A<BB1, BB2> extends B<BB1, List<BB2>> { }",
        "  interface B<CC1, CC2> extends C<CC1, CC2> { }",
        "  interface C<DD1, DD2> extends Supplier<Function<DD1, DD2>> { }",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .compilesWithoutError();
  }

  @Test
  void mapperInvalidComplicatedTree() {
    JavaFileObject javaFile = fromSource(
        "@CommandLineArguments",
        "abstract class Arguments {",
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
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("There is a problem with the mapper class: The supplied function must take a String argument, but takes Integer.");
  }

  @Test
  void mapperInvalidNotFunction() {
    JavaFileObject javaFile = fromSource(
        "@CommandLineArguments",
        "abstract class Arguments {",
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
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("There is a problem with the mapper class: not a Function or Supplier<Function>.");
  }

  @Test
  void mapperInvalidFunctionReturnType() {
    JavaFileObject javaFile = fromSource(
        "@CommandLineArguments",
        "abstract class Arguments {",
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
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("There is a problem with the mapper class: The mapper should return Integer but returns Long.");
  }

  @Test
  void mapperInvalidBounds() {
    JavaFileObject javaFile = fromSource(
        "@CommandLineArguments",
        "abstract class Arguments {",
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
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("There is a problem with the mapper class: The mapper should return Integer but returns java.util.Date");
  }

  @Test
  void mapperValidBounds() {
    JavaFileObject javaFile = fromSource(
        "@CommandLineArguments",
        "abstract class Arguments {",
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
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .compilesWithoutError();
  }

  @Test
  void mapperInvalidRawFunction() {
    JavaFileObject javaFile = fromSource(
        "@CommandLineArguments",
        "abstract class Arguments {",
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
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("There is a problem with the mapper class: not a Function or Supplier<Function>");
  }

  @Test
  void mapperInvalidSupplyingTypevar() {
    JavaFileObject javaFile = fromSource(
        "@CommandLineArguments",
        "abstract class Arguments {",
        "",
        "  @Parameter(shortName = 'x', mappedBy = Mapper.class)",
        "  abstract Integer number();",
        "",
        "  static class Mapper<E> implements Supplier<E> {",
        "    public E get() {",
        "      return null;",
        "    }",
        "  }",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("There is a problem with the mapper class: not a Function or Supplier<Function>");
  }

  @Test
  void mapperValid() {
    JavaFileObject javaFile = fromSource(
        "@CommandLineArguments",
        "abstract class Arguments {",
        "",
        "  @Parameter(shortName = 'x', mappedBy = Mapper.class)",
        "  abstract List<OptionalInt> numbers();",
        "",
        "  static class Mapper implements Supplier<Function<String, OptionalInt>> {",
        "    public Function<String, OptionalInt> get() {",
        "      return s -> OptionalInt.of(1);",
        "    }",
        "  }",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .compilesWithoutError();
  }

  @Test
  void mapperValidByte() {
    JavaFileObject javaFile = fromSource(
        "@CommandLineArguments",
        "abstract class Arguments {",
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
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .compilesWithoutError();
  }

  @Test
  void mapperValidBytePrimitive() {
    JavaFileObject javaFile = fromSource(
        "@CommandLineArguments",
        "abstract class Arguments {",
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
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .compilesWithoutError();
  }

  @Test
  void mapperValidOptionalInteger() {
    JavaFileObject javaFile = fromSource(
        "@CommandLineArguments",
        "abstract class Arguments {",
        "",
        "  @Parameter(shortName = 'x', mappedBy = Mapper.class)",
        "  abstract Optional<Integer> number();",
        "",
        "  static class Mapper implements Supplier<Function<String, Integer>> {",
        "    public Function<String, Integer> get() {",
        "      return s -> 1;",
        "    }",
        "  }",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .compilesWithoutError();
  }

  @Test
  void mapperValidOptionalStringTypevar() {
    JavaFileObject javaFile = fromSource(
        "@CommandLineArguments",
        "abstract class Arguments {",
        "",
        "  @Parameter(shortName = 'x', mappedBy = Mapper.class)",
        "  abstract Optional<String> number();",
        "",
        "  static class Mapper<E> implements Supplier<Function<E, E>> {",
        "    public Function<E, E> get() {",
        "      return null;",
        "    }",
        "  }",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .compilesWithoutError();
  }

  @Test
  void mapperValidStringOptionalStringTypevar() {
    JavaFileObject javaFile = fromSource(
        "@CommandLineArguments",
        "abstract class Arguments {",
        "",
        "  @Parameter(shortName = 'x', mappedBy = Mapper.class)",
        "  abstract Optional<String> number();",
        "",
        "  static class Mapper<E> implements Supplier<Function<E, Optional<E>>> {",
        "    public Function<E, Optional<E>> get() {",
        "      return null;",
        "    }",
        "  }",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .compilesWithoutError();
  }

  @Test
  void mapperValidStringListTypevar() {
    JavaFileObject javaFile = fromSource(
        "@CommandLineArguments",
        "abstract class Arguments {",
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
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .compilesWithoutError();
  }

  @Test
  void implicitMapperOptionalInt() {
    JavaFileObject javaFile = fromSource(
        "@CommandLineArguments",
        "abstract class Arguments {",
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
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .compilesWithoutError();
  }

  @Test
  void mapperOptionalInt() {
    JavaFileObject javaFile = fromSource(
        "@CommandLineArguments",
        "abstract class Arguments {",
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
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .compilesWithoutError();
  }

  @Test
  void mapperOptionalInteger() {
    JavaFileObject javaFile = fromSource(
        "@CommandLineArguments",
        "abstract class Arguments {",
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
        "  @Parameter(shortName = 'x', mappedBy = Mapper.class)",
        "  abstract OptionalInt b();",
        "",
        "  static class Mapper implements Supplier<Function<String, Integer>> {",
        "    public Function<String, Integer> get() {",
        "      return s -> 1;",
        "    }",
        "  }",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .compilesWithoutError();
  }


  @Test
  void mapperValidListOfSet() {
    JavaFileObject javaFile = fromSource(
        "@CommandLineArguments",
        "abstract class Arguments {",
        "",
        "  @Parameter(shortName = 'x', mappedBy = Mapper.class)",
        "  abstract List<Set<Integer>> sets();",
        "",
        "  static class Mapper implements Supplier<Function<String, Set<Integer>>> {",
        "    public Function<String, Set<Integer>> get() {",
        "      return s -> Collections.singleton(Integer.valueOf(s));",
        "    }",
        "  }",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .compilesWithoutError();
  }

  @Test
  void mapperHasTypeargsImpossibleFromString() {
    JavaFileObject javaFile = fromSource(
        "@CommandLineArguments",
        "abstract class Arguments {",
        "",
        "  @Parameter(shortName = 'x',",
        "             mappedBy = Identity.class)",
        "  abstract List<Integer> ints();",
        "",
        "  static class Identity<E> implements Supplier<Function<E, E>> {",
        "    public Function<E, E> get() {",
        "      return null;",
        "    }",
        "  }",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("There is a problem with the mapper class: could not infer type parameters.");
  }
}
