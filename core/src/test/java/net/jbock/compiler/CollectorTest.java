package net.jbock.compiler;

import org.junit.jupiter.api.Test;

import javax.tools.JavaFileObject;

import static com.google.common.truth.Truth.assertAbout;
import static com.google.testing.compile.JavaSourcesSubjectFactory.javaSources;
import static java.util.Collections.singletonList;
import static net.jbock.compiler.ProcessorTest.fromSource;

class CollectorTest {

  @Test
  void collectorValidExtendsCollector() {
    JavaFileObject javaFile = fromSource(
        "@Command",
        "abstract class Arguments {",
        "",
        "  @Option(value = \"x\", collectedBy = MySupplier.class)",
        "  abstract Set<String> strings();",
        "",
        "  static class MySupplier<E> implements Supplier<SetCollector<E>> {",
        "    public SetCollector<E> get() { return null; }",
        "  }",
        "",
        "  interface SetCollector<X> extends Collector<X, Set<X>, Set<X>> { }",
        "",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("not a declared Collector");
  }

  @Test
  void genericArrayGoodHint() {
    JavaFileObject javaFile = fromSource(
        "@Command",
        "abstract class Arguments {",
        "",
        "  @Option(value = \"x\", mappedBy = M.class, collectedBy = MyCollector.class)",
        "  abstract Set<String> strings();",
        "",
        "  static class M implements Supplier<Function<String, String[]>> {",
        "    public Function<String, String[]> get() { return null; }",
        "  }",
        "",
        "  static class MyCollector<E extends String> implements Supplier<Collector<E[], ?, Set<String>>> {",
        "    public Collector<E[], ?, Set<String>> get() { return null; }",
        "  }",
        "}");

    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .compilesWithoutError();
  }

  @Test
  void genericArrayBadHint() {
    JavaFileObject javaFile = fromSource(
        "@Command",
        "abstract class Arguments {",
        "",
        "  @Option(value = \"x\", mappedBy = M.class, collectedBy = MyCollector.class)",
        "  abstract Set<String> strings();",
        "",
        "  static class M implements Supplier<Function<String, String[]>> {",
        "    public Function<String, String[]> get() { return null; }",
        "  }",
        "",
        "  static class MyCollector<E extends Integer> implements Supplier<Collector<E[], ?, Set<String>>> {",
        "    public Collector<E[], ?, Set<String>> get() { return null; }",
        "  }",
        "}");

    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("There is a problem with the mapper class: Unification failed: java.lang.String[] and E[] have different erasure.");
  }

  @Test
  void invalidNotRepeatable() {
    JavaFileObject javaFile = fromSource(
        "@Command",
        "abstract class Arguments {",
        "",
        "  @Option(value = \"x\", collectedBy = MyCollector.class)",
        "  abstract Set<String> strings();",
        "",
        "  static class MyCollector implements Supplier<Collector<String, ?, Set<String>>> {",
        "    public Collector<String, ?, Set<String>> get() { return null; }",
        "  }",
        "}");

    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .compilesWithoutError();
  }

  @Test
  void validCollectorSupplier() {
    JavaFileObject javaFile = fromSource(
        "@Command",
        "abstract class Arguments {",
        "",
        "  @Option(value = \"x\",",
        "          collectedBy = MyCollector.class)",
        "  abstract Set<String> strings();",
        "",
        "  static class MyCollector implements Supplier<Collector<String, ?, Set<String>>> {",
        "    public Collector<String, ?, Set<String>> get() { return null; }",
        "  }",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .compilesWithoutError();
  }

  @Test
  void validCollector() {
    JavaFileObject javaFile = fromSource(
        "@Command",
        "abstract class Arguments {",
        "",
        "  @Option(value = \"x\",",
        "          collectedBy = MyCollector.class)",
        "  abstract Set<String> strings();",
        "",
        "  static class MyCollector implements Collector<String, Set<String>, Set<String>> {",
        "    public Supplier<Set<String>> supplier() { return null; }",
        "    public java.util.function.BiConsumer<Set<String>, String> accumulator() { return null; }",
        "    public java.util.function.BinaryOperator<Set<String>> combiner() { return null; }",
        "    public Function<Set<String>, Set<String>> finisher() { return null; }",
        "    public Set<Characteristics> characteristics() { return null; }",
        "  }",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .compilesWithoutError();
  }

  @Test
  void invalidReturnType() {
    JavaFileObject javaFile = fromSource(
        "@Command",
        "abstract class Arguments {",
        "",
        "  @Option(value = \"x\",",
        "          collectedBy = MyCollector.class)",
        "  abstract Set<String> strings();",
        "",
        "  static class MyCollector implements Supplier<Collector<String, ?, List<String>>> {",
        "    public Collector<String, ?, List<String>> get() { return null; }",
        "  }",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("collector");
  }

  @Test
  void invalidMapperMismatch() {
    JavaFileObject javaFile = fromSource(
        "@Command",
        "abstract class Arguments {",
        "",
        "  @Option(value = \"x\",",
        "          mappedBy = HexMapper.class,",
        "          collectedBy = ToSetCollector.class)",
        "  abstract Set<String> bigIntegers();",
        "",
        "  static class HexMapper implements Supplier<Function<String, BigInteger>> {",
        "    public Function<String, BigInteger> get() { return null; }",
        "  }",
        "",
        "  static class ToSetCollector<E> implements Supplier<Collector<E, ?, Set<E>>> {",
        "    public Collector<E, ?, Set<E>> get() { return null; }",
        "  }",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("problem");
  }

  @Test
  void validMapperMatch() {
    JavaFileObject javaFile = fromSource(
        "@Command",
        "abstract class Arguments {",
        "",
        "  @Option(value = \"x\",",
        "          mappedBy = HexMapper.class,",
        "          collectedBy = ToSetCollector.class)",
        "  abstract Set<int[]> foo();",
        "",
        "  static class HexMapper implements Supplier<Function<String, int[]>> {",
        "    public Function<String, int[]> get() { return null; }",
        "  }",
        "",
        "  static class ToSetCollector<E> implements Supplier<Collector<E, ?, Set<E>>> {",
        "    public Collector<E, ?, Set<E>> get() { return null; }",
        "  }",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .compilesWithoutError();
  }

  @Test
  void validSetAutoMapper() {
    JavaFileObject javaFile = fromSource(
        "@Command",
        "abstract class Arguments {",
        "",
        "  @Option(value = \"x\",",
        "          collectedBy = MyCollector.class)",
        "  abstract Set<java.math.BigInteger> bigIntegers();",
        "",
        "  static class MyCollector implements Supplier<Collector<java.math.BigInteger, ?, Set<java.math.BigInteger>>> {",
        "    public Collector<java.math.BigInteger, ?, Set<java.math.BigInteger>> get() {  return null; }",
        "  }",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .compilesWithoutError();
  }

  @Test
  void validGenericCollector() {
    JavaFileObject javaFile = fromSource(
        "@Command",
        "abstract class Arguments {",
        "",
        "  @Option(value = \"x\",",
        "          mappedBy = CustomBigIntegerMapper.class,",
        "          collectedBy = ToSetCollector.class)",
        "  abstract Set<java.math.BigInteger> bigSet();",
        "",
        "  static class CustomBigIntegerMapper implements Supplier<Function<String, java.math.BigInteger>> {",
        "    public Function<String, java.math.BigInteger> get() { return null; }",
        "  }",
        "",
        "  static class ToSetCollector<E> implements Supplier<Collector<E, ?, Set<E>>> {",
        "    public Collector<E, ?, Set<E>> get() {  return null; }",
        "  }",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .compilesWithoutError();
  }

  @Test
  void validGenericCollectorAutoMapper() {
    JavaFileObject javaFile = fromSource(
        "@Command",
        "abstract class Arguments {",
        "",
        "  @Option(value = \"x\",",
        "          collectedBy = ToSetCollector.class)",
        "  abstract Set<java.math.BigInteger> bigSet();",
        "",
        "  static class ToSetCollector<E> implements Supplier<Collector<E, ?, Set<E>>> {",
        "    public Collector<E, ?, Set<E>> get() {  return null; }",
        "  }",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .compilesWithoutError();
  }

  @Test
  void validMap() {
    JavaFileObject javaFile = fromSource(
        "@Command",
        "abstract class Arguments {",
        "",
        "  @Option(value = \"x\",",
        "          mappedBy = MapEntryMapper.class,",
        "          collectedBy = MapEntryCollector.class)",
        "  abstract java.util.Map<String, java.time.LocalDate> map();",
        "",
        "  static class MapEntryMapper implements Supplier<Function<String, java.util.Map.Entry<String, java.time.LocalDate>>> {",
        "    public Function<String, java.util.Map.Entry<String, java.time.LocalDate>> get() { return null; }",
        "  }",
        "",
        "  static class MapEntryCollector<K, V> implements Supplier<Collector<java.util.Map.Entry<K, V>, ?, java.util.Map<K, V>>> {",
        "    public Collector<java.util.Map.Entry<K, V>, ?, java.util.Map<K, V>> get() { return null; }",
        "  }",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .compilesWithoutError();
  }

  @Test
  void validBothMapperAndCollectorHaveTypeargs() {
    JavaFileObject javaFile = fromSource(
        "@Command",
        "abstract class Arguments {",
        "",
        "  @Option(value = \"x\",",
        "          mappedBy = IdentityMapper.class,",
        "          collectedBy = ToListCollector.class)",
        "  abstract List<String> map();",
        "",
        "  static class IdentityMapper<M> implements Supplier<Function<M, M>> {",
        "    public Function<M, M> get() { return null; }",
        "  }",
        "",
        "  static class ToListCollector<E> implements Supplier<Collector<E, ?, List<E>>> {",
        "    public Collector<E, ?, List<E>> get() { return null; }",
        "  }",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .compilesWithoutError();
  }

  @Test
  void invalidOptionalAuto() {
    JavaFileObject javaFile = fromSource(
        "@Command",
        "abstract class Arguments {",
        "",
        "  @Option(value = \"x\",",
        "          collectedBy = ToSetCollector.class)",
        "  abstract Set<Optional<Integer>> optionalIntegers();",
        "",
        "  static class ToSetCollector<E> implements Supplier<Collector<E, ?, Set<E>>> {",
        "    public Collector<E, ?, Set<E>> get() { return null; }",
        "  }",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("Unknown parameter type: java.util.Optional<java.lang.Integer>. Try defining a custom mapper.");
  }

  @Test
  void invalidBounds() {
    JavaFileObject javaFile = fromSource(
        "@Command",
        "abstract class Arguments {",
        "",
        "  @Option(value = \"x\",",
        "          collectedBy = ToSetCollector.class)",
        "  abstract Set<Integer> integers();",
        "",
        "  static class ToSetCollector<E extends Long> implements Supplier<Collector<E, ?, Set<E>>> {",
        "    public Collector<E, ?, Set<E>> get() { return null; }",
        "  }",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("There is a problem with the collector class: Invalid bounds: Can't resolve E to java.lang.Integer.");
  }

  @Test
  void freeTypeVariableInCollectorIntersectionType() {
    JavaFileObject javaFile = fromSource(
        "@Command",
        "abstract class Arguments {",
        "",
        "  @Option(value = \"x\",",
        "          collectedBy = MyCollector.class)",
        "  abstract Set<Integer> integers();",
        "",
        "  static class MyCollector<E extends Long & Number, F> implements Supplier<Collector<E, ?, Set<F>>> {",
        "    public Collector<E, ?, Set<F>> get() { return null; }",
        "  }",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("There is a problem with the collector class: Intersection type is not supported for typevar E.");
  }

  @Test
  void freeTypeVariableInCollectorMapped() {
    JavaFileObject javaFile = fromSource(
        "@Command",
        "abstract class Arguments {",
        "",
        "  @Option(value = \"x\",",
        "          mappedBy = MyMapper.class,",
        "          collectedBy = MyCollector.class)",
        "  abstract Set<Integer> integers();",
        "",
        "  static class MyMapper implements Supplier<Function<String, Long>> {",
        "    public Function<String, Long> get() { return null; }",
        "  }",
        "",
        "  static class MyCollector<E, F> implements Supplier<Collector<E, ?, Set<F>>> {",
        "    public Collector<E, ?, Set<F>> get() { return null; }",
        "  }",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .compilesWithoutError();
  }

  @Test
  void freeTypeVariableInCollectorMappedIncompatible() {
    JavaFileObject javaFile = fromSource(
        "@Command",
        "abstract class Arguments {",
        "",
        "  @Option(value = \"x\",",
        "          mappedBy = MyMapper.class,",
        "          collectedBy = MyCollector.class)",
        "  abstract Set<Integer> integers();",
        "",
        "  static class MyMapper implements Supplier<Function<String, Long>> {",
        "    public Function<String, Long> get() { return null; }",
        "  }",
        "",
        "  static class MyCollector<E extends String, F> implements Supplier<Collector<E, ?, Set<F>>> {",
        "    public Collector<E, ?, Set<F>> get() { return null; }",
        "  }",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("There is a problem with the mapper class: Unification failed: can't assign java.lang.Long to java.lang.String.");
  }

  @Test
  void freeTypeVariableInMapperAndCollector() {
    JavaFileObject javaFile = fromSource(
        "@Command",
        "abstract class Arguments {",
        "",
        "  @Option(value = \"x\",",
        "          mappedBy = MyMapper.class,",
        "          collectedBy = MyCollector.class)",
        "  abstract Set<Integer> integers();",
        "",
        "  static class MyMapper<A> implements Supplier<Function<String, A>> {",
        "    public Function<String, A> get() { return null; }",
        "  }",
        "",
        "  static class MyCollector<E extends String, F> implements Supplier<Collector<E, ?, Set<F>>> {",
        "    public Collector<E, ?, Set<F>> get() { return null; }",
        "  }",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .compilesWithoutError();
  }

  @Test
  void freeTypeVariableInMapperAndCollectorIncompatible() {
    JavaFileObject javaFile = fromSource(
        "@Command",
        "abstract class Arguments {",
        "",
        "  @Option(value = \"x\",",
        "          mappedBy = MyMapper.class,",
        "          collectedBy = MyCollector.class)",
        "  abstract Set<Integer> integers();",
        "",
        "  static class MyMapper<A extends Long> implements Supplier<Function<String, A>> {",
        "    public Function<String, A> get() { return null; }",
        "  }",
        "",
        "  static class MyCollector<E extends String, F> implements Supplier<Collector<E, ?, Set<F>>> {",
        "    public Collector<E, ?, Set<F>> get() { return null; }",
        "  }",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("There is a problem with the mapper class: Invalid bounds: Can't resolve A to java.lang.String.");
  }

  @Test
  void invalidOptionalIntAuto() {
    JavaFileObject javaFile = fromSource(
        "@Command",
        "abstract class Arguments {",
        "",
        "  @Option(value = \"x\",",
        "          collectedBy = ToSetCollector.class)",
        "  abstract Set<java.util.OptionalInt> optionalInts();",
        "",
        "  static class ToSetCollector<E> implements Supplier<Collector<E, ?, Set<E>>> {",
        "    public Collector<E, ?, Set<E>> get() { return null; }",
        "  }",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor(true))
        .failsToCompile()
        .withErrorContaining("Unknown parameter type: java.util.OptionalInt. Try defining a custom mapper.");
  }

  @Test
  void invalidBoundSupplier() {
    JavaFileObject javaFile = fromSource(
        "@Command",
        "abstract class Arguments {",
        "",
        "  @Option(value = \"x\",",
        "          collectedBy = A.class)",
        "  abstract Set<String> strings();",
        "",
        "  static class A implements ToSetCollector<Long> {",
        "    public Collector<Long, ?, Set<Long>> get() { return null; }",
        "  }",
        "",
        "  interface ToSetCollector<E> extends Supplier<Collector<E, ?, Set<E>>> { }",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("There is a problem with the collector class: Unification failed: can't assign java.lang.Long to java.lang.String.");
  }

  @Test
  void invalidBound() {
    JavaFileObject javaFile = fromSource(
        "@Command",
        "abstract class Arguments {",
        "",
        "  @Option(value = \"x\",",
        "          collectedBy = A.class)",
        "  abstract Set<String> strings();",
        "",
        "  static class A implements ToSetCollector<Long> {",
        "    public Supplier<Set<Long>> supplier() { return null; }",
        "    public BiConsumer<Set<Long>, Long> accumulator() { return null; }",
        "    public BinaryOperator<Set<Long>> combiner() { return null; }",
        "    public Function<Set<Long>, Set<Long>> finisher() { return null; }",
        "    public Set<Characteristics> characteristics() { return null; }",
        "  }",
        "",
        "  interface ToSetCollector<E> extends Collector<E, Set<E>, Set<E>> { }",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("There is a problem with the collector class: Unification failed: can't assign java.lang.Long to java.lang.String.");
  }

  @Test
  void freeTypeVariableInMapperAndCollectorCollectorPreference() {
    JavaFileObject javaFile = fromSource(
        "@Command",
        "abstract class Arguments {",
        "",
        "  @Option(value = \"x\",",
        "             mappedBy = Map.class,",
        "             collectedBy = Collect.class)",
        "  abstract List<Integer> map();",
        "",
        "  static class Map<E extends CharSequence, F> implements Supplier<Function<E, F>> {",
        "    public Function<E, F> get() { return null; }",
        "  }",
        "",
        "  static class Collect<E extends Number> implements Supplier<Collector<E, ?, List<E>>> {",
        "    public Collector<E, ?, List<E>> get() { return null; }",
        "  }",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .compilesWithoutError();
  }

  @Test
  void freeTypeVariableInCollector() {
    JavaFileObject javaFile = fromSource(
        "@Command",
        "abstract class Arguments {",
        "",
        "  @Option(value = \"x\",",
        "          collectedBy = MyCollector.class)",
        "  abstract Set<Integer> integers();",
        "",
        "  static class MyCollector<E extends String, F> implements Supplier<Collector<E, ?, Set<F>>> {",
        "    public Collector<E, ?, Set<F>> get() { return null; }",
        "  }",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .compilesWithoutError();
  }

  @Test
  void validFreeTypevarsInMapperAndCollectorMapperPreferencePossibleNumberToInteger() {
    JavaFileObject javaFile = fromSource(
        "@Command",
        "abstract class Arguments {",
        "",
        "  @Option(value = \"x\",",
        "          mappedBy = Map.class,",
        "          collectedBy = Collect.class)",
        "  abstract List<Integer> map();",
        "",
        "  static class Map<E , F extends Number> implements Supplier<Function<E, List<F>>> {",
        "    public Function<E, List<F>> get() { return null; }",
        "  }",
        "",
        "  static class Collect<F extends Integer, E> implements Supplier<Collector<List<F>, ?, List<E>>> {",
        "    public Collector<List<F>, ?, List<E>> get() { return null; }",
        "  }",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .compilesWithoutError();
  }

  @Test
  void validBothMapperCollectorAndResultHaveTypeargs() {
    JavaFileObject javaFile = fromSource(
        "@Command",
        "abstract class Arguments {",
        "",
        "  @Option(value = \"x\",",
        "          mappedBy = Map.class,",
        "          collectedBy = Collect.class)",
        "  abstract List<Result<String>> map();",
        "",
        "  static class Map<E, F extends java.util.Collection> implements Supplier<Function<E, F>> {",
        "    public Function<E, F> get() { return null; }",
        "  }",
        "",
        "  static class Collect<E extends Result> implements Supplier<Collector<Set<E>, ?, List<E>>> {",
        "    public Collector<Set<E>, ?, List<E>> get() { return null; }",
        "  }",
        "",
        "  static class Result<E extends java.lang.CharSequence> {}",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .compilesWithoutError();
  }

  @Test
  void bothMapperAndCollectorHaveTypeargsInvalidBoundsOnCollector() {
    JavaFileObject javaFile = fromSource(
        "@Command",
        "abstract class Arguments {",
        "",
        "  @Option(value = \"x\",",
        "          mappedBy = XMap.class,",
        "          collectedBy = YCol.class)",
        "  abstract List<String> map();",
        "",
        "  static class XMap<A> implements Supplier<Function<A, A>> {",
        "    public Function<A, A> get() { return null; }",
        "  }",
        "",
        "  static class YCol<E extends Integer> implements Supplier<Collector<E, ?, List<E>>> {",
        "    public Collector<E, ?, List<E>> get() { return null; }",
        "  }",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("There is a problem with the collector class: Invalid bounds: Can't resolve E to java.lang.String.");
  }


  @Test
  void bothMapperAndCollectorHaveTypeargsImpossibleFromString() {
    JavaFileObject javaFile = fromSource(
        "@Command",
        "abstract class Arguments {",
        "",
        "  @Option(value = \"x\",",
        "          mappedBy = Identity.class,",
        "          collectedBy = Collect.class)",
        "  abstract List<Integer> ints();",
        "",
        "  static class Identity<E> implements Supplier<Function<E, E>> {",
        "    public Function<E, E> get() { return null; }",
        "  }",
        "",
        "  static class Collect<E> implements Supplier<Collector<E, ?, List<E>>> {",
        "    public Collector<E, ?, List<E>> get() { return null; }",
        "  }",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("There is a problem with the mapper class: Cannot infer E: java.lang.String vs java.lang.Integer.");
  }

  @Test
  void bothMapperAndCollectorHaveTypeargsValid() {
    JavaFileObject javaFile = fromSource(
        "@Command",
        "abstract class Arguments {",
        "",
        "  @Option(value = \"x\",",
        "          mappedBy = MakeList.class,",
        "          collectedBy = Concat.class)",
        "  abstract List<String> strings();",
        "",
        "  static class MakeList<E> implements Supplier<Function<E, List<E>>> {",
        "    public Function<E, List<E>> get() { return null; }",
        "  }",
        "",
        "  static class Concat<E> implements Supplier<Collector<List<E>, ?, List<E>>> {",
        "    public Collector<List<E>, ?, List<E>> get() { return null; }",
        "  }",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .compilesWithoutError();
  }

  @Test
  void validEnum() {
    JavaFileObject javaFile = fromSource(
        "@Command",
        "abstract class Arguments {",
        "",
        "  @Option(value = \"x\", collectedBy = ToSetCollector.class)",
        "  abstract Set<Foo> foo();",
        "",
        "  enum Foo {",
        "    BAR",
        "   }",
        "",
        "  static class ToSetCollector<E> implements Supplier<Collector<E, ?, Set<E>>> {",
        "    public Collector<E, ?, Set<E>> get() { return null; }",
        "  }",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .compilesWithoutError();
  }

  @Test
  void invalidEnum() {
    JavaFileObject javaFile = fromSource(
        "@Command",
        "abstract class Arguments {",
        "",
        "  @Option(value = \"x\", collectedBy = ToSetCollector.class)",
        "  abstract Foo foo();",
        "",
        "  enum Foo {",
        "    BAR",
        "   }",
        "",
        "  static class ToSetCollector<E> implements Supplier<Collector<E, ?, Set<E>>> {",
        "    public Collector<E, ?, Set<E>> get() { return null; }",
        "  }",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("There is a problem with the collector class: Unification failed: can't assign java.util.Set<E> to test.Arguments.Foo.");
  }

  @Test
  void collectorInvalidNotCollector() {
    JavaFileObject javaFile = fromSource(
        "@Command",
        "abstract class Arguments {",
        "",
        "  @Option(value = \"x\", collectedBy = ZapperSupplier.class)",
        "  abstract String zap();",
        "",
        "  static class ZapperSupplier implements Supplier<String> { }",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("not a declared Collector");
  }
}
