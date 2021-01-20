package net.jbock.compiler;

import org.junit.jupiter.api.Test;

import javax.tools.JavaFileObject;

import static com.google.common.truth.Truth.assertAbout;
import static com.google.testing.compile.JavaSourcesSubjectFactory.javaSources;
import static java.util.Collections.singletonList;
import static net.jbock.compiler.ProcessorTest.fromSource;

class MapperTest {

  @Test
  void missingMapperAnnotation() {
    JavaFileObject javaFile = fromSource(
        "class MapMap implements Function<String, String> {",
        "  public String apply(String s) { return null; }",
        "}",
        "",
        "@Command",
        "abstract class Arguments {",
        "",
        "  @Option(value = \"x\", mappedBy = MapMap.class)",
        "  abstract String foo();",
        "",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("There is a problem with the mapper class: The class must either be an inner class of test.Arguments, or carry the net.jbock.Mapper annotation.");
  }

  @Test
  void validArrayMapperSupplier() {
    JavaFileObject javaFile = fromSource(
        "@Command",
        "abstract class Arguments {",
        "",
        "  @Option(value = \"x\", mappedBy = ArrayMapper.class)",
        "  abstract Optional<int[]> foo();",
        "",
        "  @Mapper",
        "  static class ArrayMapper implements Supplier<Function<String, int[]>> {",
        "    public Function<String, int[]> get() { return null; }",
        "  }",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .compilesWithoutError();
  }

  @Test
  void validArrayMapper() {
    JavaFileObject javaFile = fromSource(
        "@Command",
        "abstract class Arguments {",
        "",
        "  @Option(value = \"x\", mappedBy = ArrayMapper.class)",
        "  abstract Optional<int[]> foo();",
        "",
        "  @Mapper",
        "  static class ArrayMapper implements Function<String, int[]> {",
        "    public int[] apply(String s) { return null; }",
        "  }",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .compilesWithoutError();
  }

  @Test
  void invalidFlagMapper() {
    JavaFileObject javaFile = fromSource(
        "@Command",
        "abstract class Arguments {",
        "",
        "  @Option(value = \"x\", mappedBy = FlagMapper.class)",
        "  abstract Boolean flag();",
        "",
        "  @Mapper",
        "  static class FlagMapper implements Supplier<Function<String, Boolean>> {",
        "    public Function<String, Boolean> get() { return null; }",
        "  }",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .compilesWithoutError();
  }

  @Test
  void validBooleanList() {
    JavaFileObject javaFile = fromSource(
        "@Command",
        "abstract class Arguments {",
        "",
        "  @Param(value = 0, mappedBy = BooleanMapper.class)",
        "  abstract List<Boolean> booleanList();",
        "",
        "  @Mapper",
        "  static class BooleanMapper implements Supplier<Function<String, Boolean>> {",
        "    public Function<String, Boolean> get() { return null; }",
        "  }",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .compilesWithoutError();
  }

  @Test
  void invalidBounds() {
    JavaFileObject javaFile = fromSource(
        "@Command",
        "abstract class Arguments {",
        "",
        "  @Param(value = 1, mappedBy = BoundMapper.class)",
        "  abstract String a();",
        "",
        "  @Mapper",
        "  static class BoundMapper<E extends Integer> implements Supplier<Function<E, E>> {",
        "    public Function<E, E> get() { return null; }",
        "  }",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("mapper");
  }

  @Test
  void validBounds() {
    JavaFileObject javaFile = fromSource(
        "@Command",
        "abstract class Arguments {",
        "",
        "  @Param(value = 1, mappedBy = BoundMapper.class)",
        "  abstract String a();",
        "",
        "  @Mapper",
        "  static class BoundMapper implements Katz<String> {",
        "    public Function<String, String> get() { return null; }",
        "  }",
        "",
        "  interface Katz<OR> extends Supplier<Function<OR, OR>> { }",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("not a java.util.function.Function or java.util.function.Supplier<java.util.function.Function>");
  }

  @Test
  void mapperInvalidPrivateConstructor() {
    JavaFileObject javaFile = fromSource(
        "@Command",
        "abstract class Arguments {",
        "",
        "  @Option(value = \"x\", mappedBy = MapMap.class)",
        "  abstract Integer number();",
        "",
        "  @Mapper",
        "  static class MapMap implements Supplier<Function<String, Integer>> {",
        "",
        "    private MapMap() {}",
        "",
        "    public Function<String, Integer> get() { return null; }",
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
        "@Command",
        "abstract class Arguments {",
        "",
        "  @Option(value = \"x\", mappedBy = MapMap.class)",
        "  abstract Integer number();",
        "",
        "  @Mapper",
        "  static class MapMap implements Supplier<Function<String, Integer>> {",
        "",
        "    MapMap(int i) {}",
        "",
        "    public Function<String, Integer> get() { return null; }",
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
        "@Command",
        "abstract class Arguments {",
        "",
        "  @Option(value = \"x\", mappedBy = MapMap.class)",
        "  abstract Integer number();",
        "",
        "  @Mapper",
        "  static class MapMap implements Supplier<Function<String, Integer>> {",
        "",
        "    MapMap() throws IllegalStateException {}",
        "",
        "    public Function<String, Integer> get() { return null; }",
        "  }",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("The class must have a default constructor");
  }

  @Test
  void mapperInvalidNonstaticInnerClass() {
    JavaFileObject javaFile = fromSource(
        "@Command",
        "abstract class Arguments {",
        "",
        "  @Option(value = \"x\", mappedBy = MapMap.class)",
        "  abstract Integer number();",
        "",
        "  @Mapper",
        "  class MapMap implements Supplier<Function<String, Integer>> {",
        "    public Function<String, Integer> get() { return null; }",
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
        "@Command",
        "abstract class Arguments {",
        "",
        "  @Option(value = \"x\", mappedBy = MapMap.class)",
        "  abstract Integer number();",
        "",
        "  @Mapper",
        "  static class MapMap implements Supplier<Function<Integer, Integer>> {",
        "    public Function<Integer, Integer> get() { return null; }",
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
        "@Command",
        "abstract class Arguments {",
        "",
        "  @Option(value = \"x\", mappedBy = MapMap.class)",
        "  abstract Integer number();",
        "",
        "  @Mapper",
        "  static class MapMap implements Supplier<Function<String, String>> {",
        "    public Function<String, String> get() { return null; }",
        "  }",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("There is a problem with the mapper class: No match. Try returning java.lang.Integer from the mapper.");
  }

  @Test
  void mapperInvalidReturnsStringOptional() {
    JavaFileObject javaFile = fromSource(
        "@Command",
        "abstract class Arguments {",
        "",
        "  @Option(value = \"x\", mappedBy = MapMap.class)",
        "  abstract java.util.OptionalInt number();",
        "",
        "  @Mapper",
        "  static class MapMap implements Supplier<Function<String, String>> {",
        "    public Function<String, String> get() { return null; }",
        "  }",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("There is a problem with the mapper class: No match. Try returning java.lang.Integer from the mapper.");
  }

  @Test
  void mapperInvalidReturnsStringList() {
    JavaFileObject javaFile = fromSource(
        "@Command",
        "abstract class Arguments {",
        "",
        "  @Option(value = \"x\", mappedBy = MapMap.class)",
        "  abstract List<Integer> number();",
        "",
        "  @Mapper",
        "  static class MapMap implements Supplier<Function<String, String>> {",
        "    public Function<String, String> get() { return null; }",
        "  }",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("There is a problem with the mapper class: No match. Try returning java.lang.Integer from the mapper.");
  }

  @Test
  void rawType() {
    JavaFileObject javaFile = fromSource(
        "@Command",
        "abstract class Arguments {",
        "",
        "  @Option(value = \"x\", mappedBy = MapMap.class)",
        "  abstract java.util.Set<java.util.Set> things();",
        "",
        "  @Mapper",
        "  static class MapMap implements Supplier<Function<String, java.util.Set<java.util.Set>>> {",
        "    public Function<String, java.util.Set<java.util.Set>> get() { return null; }",
        "  }",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .compilesWithoutError();
  }

  @Test
  void mapperValidTypevars() {
    JavaFileObject javaFile = fromSource(
        "@Command",
        "abstract class Arguments {",
        "",
        "  @Option(value = \"x\", mappedBy = MapMap.class)",
        "  abstract Supplier<String> string();",
        "",
        "  @Mapper",
        "  static class MapMap implements Supplier<Function<String, Supplier<String>>> {",
        "    public Function<String, Supplier<String>> get() { return null; }",
        "  }",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .compilesWithoutError();
  }

  @Test
  void mapperValidNestedTypevars() {
    JavaFileObject javaFile = fromSource(
        "@Command",
        "abstract class Arguments {",
        "",
        "  @Option(value = \"x\", mappedBy = MapMap.class)",
        "  abstract Supplier<Optional<String>> string();",
        "",
        "  @Mapper",
        "  static class MapMap implements Supplier<Function<String, Supplier<Optional<String>>>> {",
        "    public Function<String, Supplier<Optional<String>>> get() { return null; }",
        "  }",
        "}");

    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .compilesWithoutError();
  }

  @Test
  void mapperValidExtendsFunction() {
    JavaFileObject javaFile = fromSource(
        "@Command",
        "abstract class Arguments {",
        "",
        "  @Option(value = \"x\", mappedBy = MapMap.class)",
        "  abstract Integer number();",
        "",
        "  @Mapper",
        "  static class MapMap implements Supplier<StringFunction<String, Integer>> {",
        "    public StringFunction<String, Integer> get() { return null; }",
        "  }",
        "",
        "  interface StringFunction<V, X> extends Function<V, X> {}",
        "",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("expected java.util.function.Function but found test.Arguments.StringFunction<java.lang.String,java.lang.Integer>");
  }

  @Test
  void mapperInvalidStringFunction() {
    JavaFileObject javaFile = fromSource(
        "@Command",
        "abstract class Arguments {",
        "",
        "  @Option(value = \"x\", mappedBy = MapMap.class)",
        "  abstract Integer number();",
        "",
        "  @Mapper",
        "  static class MapMap implements Supplier<StringFunction<Integer>> {",
        "    public StringFunction<Integer> get() { return null; }",
        "  }",
        "",
        "  interface StringFunction<R> extends Function<Long, R> {}",
        "",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("expected java.util.function.Function but found test.Arguments.StringFunction<java.lang.Integer>");
  }

  @Test
  void testMapperTypeSudokuInvalid() {
    JavaFileObject javaFile = fromSource(
        "@Command",
        "abstract class Arguments {",
        "",
        "  @Option(value = \"x\", mappedBy = MapMap.class)",
        "  abstract List<List<Integer>> number();",
        "",
        "  @Mapper",
        "  static class MapMap<E extends List<List<Integer>>> implements FooSupplier<E> { public Foo<E> get() { return null; } }",
        "  interface FooSupplier<K> extends Supplier<Foo<K>> { }",
        "  interface Foo<X> extends Function<String, List<List<X>>> { }",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("There is a problem with the mapper class");
  }

  @Test
  void testSudokuHard() {
    JavaFileObject javaFile = fromSource(
        "@Command",
        "abstract class Arguments {",
        "",
        "  @Option(value = \"x\", mappedBy = MapMap.class)",
        "  abstract java.util.ArrayList<List<List<List<List<List<List<Set<Set<Set<Set<Set<Set<java.util.Collection<Integer>>>>>>>>>>>>>> numbers();",
        "",
        "  @Mapper",
        "  static class MapMap implements Supplier<Function<String, java.util.ArrayList<List<List<List<List<List<List<Set<Set<Set<Set<Set<Set<java.util.Collection<Integer>>>>>>>>>>>>>>>> {",
        "    public Foo1<Set<Set<Set<Set<Set<Set<java.util.Collection<Integer>>>>>>>> get() { return null; }",
        "  }",
        "  interface Foo1<A> extends Foo2<List<A>> { }",
        "  interface Foo2<B> extends Foo3<List<B>> { }",
        "  interface Foo3<C> extends Foo4<List<C>> { }",
        "  interface Foo4<D> extends Foo5<List<D>> { }",
        "  interface Foo5<E> extends Foo6<List<E>> { }",
        "  interface Foo6<F> extends Foo7<List<F>> { }",
        "  interface Foo7<G> extends Foo8<java.util.ArrayList<G>> { }",
        "  interface Foo8<H> extends Function<String, H> { }",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .compilesWithoutError();
  }

  @Test
  void mapperInvalidRawFunctionSupplier() {
    JavaFileObject javaFile = fromSource(
        "@Command",
        "abstract class Arguments {",
        "",
        "  @Option(value = \"x\", mappedBy = MapMap.class)",
        "  abstract Integer number();",
        "",
        "  @Mapper",
        "  static class MapMap implements Supplier<Function> {",
        "    public Function get() { return null; }",
        "  }",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("There is a problem with the mapper class: raw type: java.util.function.Function.");
  }

  @Test
  void mapperInvalidRawSupplier() {
    JavaFileObject javaFile = fromSource(
        "@Command",
        "abstract class Arguments {",
        "",
        "  @Option(value = \"x\", mappedBy = MapMap.class)",
        "  abstract Integer number();",
        "",
        "  @Mapper",
        "  static class MapMap implements Supplier {",
        "    public Object get() { return null; }",
        "  }",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("There is a problem with the mapper class: raw type: java.util.function.Supplier.");
  }

  @Test
  void mapperInvalidRawFunction() {
    JavaFileObject javaFile = fromSource(
        "@Command",
        "abstract class Arguments {",
        "",
        "  @Option(value = \"x\", mappedBy = MapMap.class)",
        "  abstract Integer number();",
        "",
        "  @Mapper",
        "  static class MapMap implements Function {",
        "    public Object apply(Object o) { return null; }",
        "  }",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("There is a problem with the mapper class: raw type: java.util.function.Function.");
  }

  @Test
  void mapperValid() {
    JavaFileObject javaFile = fromSource(
        "@Command",
        "abstract class Arguments {",
        "",
        "  @Option(value = \"x\", mappedBy = MapMap.class)",
        "  abstract List<java.util.OptionalInt> numbers();",
        "",
        "  @Mapper",
        "  static class MapMap implements Supplier<Function<String, java.util.OptionalInt>> {",
        "    public Function<String, java.util.OptionalInt> get() { return null; }",
        "  }",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .compilesWithoutError();
  }

  @Test
  void mapperValidByte() {
    JavaFileObject javaFile = fromSource(
        "@Command",
        "abstract class Arguments {",
        "",
        "  @Option(value = \"x\", mappedBy = MapMap.class)",
        "  abstract Byte number();",
        "",
        "  @Mapper",
        "  static class MapMap implements Supplier<Function<String, Byte>> {",
        "    public Function<String, Byte> get() { return null; }",
        "  }",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .compilesWithoutError();
  }

  @Test
  void mapperValidBytePrimitive() {
    JavaFileObject javaFile = fromSource(
        "@Command",
        "abstract class Arguments {",
        "",
        "  @Option(value = \"x\", mappedBy = MapMap.class)",
        "  abstract byte number();",
        "",
        "  @Mapper",
        "  static class MapMap implements Supplier<Function<String, Byte>> {",
        "    public Function<String, Byte> get() { return null; }",
        "  }",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .compilesWithoutError();
  }

  @Test
  void mapperValidOptionalInteger() {
    JavaFileObject javaFile = fromSource(
        "@Command",
        "abstract class Arguments {",
        "",
        "  @Option(value = \"x\", mappedBy = MapMap.class)",
        "  abstract Optional<Integer> number();",
        "",
        "  @Mapper",
        "  static class MapMap implements Supplier<Function<String, Integer>> {",
        "    public Function<String, Integer> get() { return null; }",
        "  }",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .compilesWithoutError();
  }

  @Test
  void implicitMapperOptionalInt() {
    JavaFileObject javaFile = fromSource(
        "@Command",
        "abstract class Arguments {",
        "",
        "  @Option(value = \"x\", mappedBy = MapMap.class)",
        "  abstract java.util.OptionalInt b();",
        "",
        "  @Mapper",
        "  static class MapMap implements Supplier<Function<String, Integer>> {",
        "    public Function<String, Integer> get() { return null; }",
        "  }",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .compilesWithoutError();
  }

  @Test
  void mapperOptionalInt() {
    JavaFileObject javaFile = fromSource(
        "@Command",
        "abstract class Arguments {",
        "",
        "  @Option(value = \"x\", mappedBy = MapMap.class)",
        "  abstract java.util.OptionalInt b();",
        "",
        "  @Mapper",
        "  static class MapMap implements Supplier<Function<String, java.util.OptionalInt>> {",
        "    public Function<String, java.util.OptionalInt> get() { return null; }",
        "  }",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .compilesWithoutError();
  }

  @Test
  void mapperOptionalInteger() {
    JavaFileObject javaFile = fromSource(
        "@Command",
        "abstract class Arguments {",
        "",
        "  @Option(value = \"x\", mappedBy = MapMap.class)",
        "  abstract Optional<Integer> b();",
        "",
        "  @Mapper",
        "  static class MapMap implements Supplier<Function<String, Optional<Integer>>> {",
        "    public Function<String, Optional<Integer>> get() { return null; }",
        "  }",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .compilesWithoutError();
  }

  @Test
  void oneOptionalInt() {
    JavaFileObject javaFile = fromSource(
        "@Command",
        "abstract class Arguments {",
        "",
        "  @Option(value = \"x\", mappedBy = MapMap.class)",
        "  abstract java.util.OptionalInt b();",
        "",
        "  @Mapper",
        "  static class MapMap implements Supplier<Function<String, Integer>> {",
        "    public Function<String, Integer> get() { return null; }",
        "  }",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .compilesWithoutError();
  }


  @Test
  void mapperValidListOfSet() {
    JavaFileObject javaFile = fromSource(
        "@Command",
        "abstract class Arguments {",
        "",
        "  @Option(value = \"x\", mappedBy = MapMap.class)",
        "  abstract List<Set<Integer>> sets();",
        "",
        "  @Mapper",
        "  static class MapMap implements Supplier<Function<String, Set<Integer>>> {",
        "    public Function<String, Set<Integer>> get() { return null; }",
        "  }",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .compilesWithoutError();
  }
}
