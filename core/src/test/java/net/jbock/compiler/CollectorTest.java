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
        "@CommandLineArguments",
        "abstract class Arguments {",
        "",
        "  @Parameter(shortName = 'x', collectedBy = MySupplier.class)",
        "  abstract Set<String> strings();",
        "",
        "  static class MySupplier<E> implements Supplier<SetCollector<E>> {",
        "    public SetCollector<E> get() {",
        "      return null;",
        "    }",
        "  }",
        "",
        "  interface SetCollector<X> extends Collector<X, Set<X>, Set<X>> { }",
        "",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("not a Collector or Supplier<Collector>");
  }

  @Test
  void invalidNotRepeatable() {
    JavaFileObject javaFile = fromSource(
        "@CommandLineArguments",
        "abstract class Arguments {",
        "",
        "  @Parameter(shortName = 'x', collectedBy = MyCollector.class)",
        "  abstract Set<String> strings();",
        "",
        "  static class MyCollector implements Supplier<Collector<String, ?, Set<String>>> {",
        "    public Collector<String, ?, Set<String>> get() {",
        "      return Collectors.toSet();",
        "    }",
        "  }",
        "}");

    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .compilesWithoutError();
  }

  @Test
  void invalidCollectorClassDoesNotExist() {
    JavaFileObject javaFile = fromSource(
        "@CommandLineArguments",
        "abstract class Arguments {",
        "",
        "  @Parameter(shortName = 'x',",
        "             collectedBy = JustNotExist.class)",
        "  abstract Set<String> strings();",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("Invalid value of 'collectedBy'.");
  }

  @Test
  void validCollectorSupplier() {
    JavaFileObject javaFile = fromSource(
        "@CommandLineArguments",
        "abstract class Arguments {",
        "",
        "  @Parameter(shortName = 'x',",
        "             collectedBy = MyCollector.class)",
        "  abstract Set<String> strings();",
        "",
        "  static class MyCollector implements Supplier<Collector<String, ?, Set<String>>> {",
        "    public Collector<String, ?, Set<String>> get() {",
        "      return Collectors.toSet();",
        "    }",
        "  }",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .compilesWithoutError();
  }

  @Test
  void validCollector() {
    JavaFileObject javaFile = fromSource(
        "@CommandLineArguments",
        "abstract class Arguments {",
        "",
        "  @Parameter(shortName = 'x',",
        "             collectedBy = MyCollector.class)",
        "  abstract Set<String> strings();",
        "",
        "  static class MyCollector implements Collector<String, Set<String>, Set<String>> {",
        "    public Supplier<Set<String>> supplier() { return null; }",
        "    public BiConsumer<Set<String>, String> accumulator() { return null; }",
        "    public BinaryOperator<Set<String>> combiner() { return null; }",
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
        "@CommandLineArguments",
        "abstract class Arguments {",
        "",
        "  @Parameter(shortName = 'x',",
        "             collectedBy = MyCollector.class)",
        "  abstract Set<String> strings();",
        "",
        "  static class MyCollector implements Supplier<Collector<String, ?, List<String>>> {",
        "    public Collector<String, ?, List<String>> get() {",
        "      return Collectors.toList();",
        "    }",
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
        "@CommandLineArguments",
        "abstract class Arguments {",
        "",
        "  @Parameter(shortName = 'x',",
        "             mappedBy = HexMapper.class,",
        "             collectedBy = ToSetCollector.class)",
        "  abstract Set<String> bigIntegers();",
        "",
        "  static class HexMapper implements Supplier<Function<String, BigInteger>> {",
        "    public Function<String, BigInteger> get() {",
        "      return s -> new BigInteger(s, 16);",
        "    }",
        "  }",
        "",
        "  static class ToSetCollector<E> implements Supplier<Collector<E, ?, Set<E>>> {",
        "    public Collector<E, ?, Set<E>> get() {",
        "      return Collectors.toSet();",
        "    }",
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
        "@CommandLineArguments",
        "abstract class Arguments {",
        "",
        "  @Parameter(shortName = 'x',",
        "             mappedBy = HexMapper.class,",
        "             collectedBy = ToSetCollector.class)",
        "  abstract Set<int[]> foo();",
        "",
        "  static class HexMapper implements Supplier<Function<String, int[]>> {",
        "    public Function<String, int[]> get() {",
        "      return null;",
        "    }",
        "  }",
        "",
        "  static class ToSetCollector<E> implements Supplier<Collector<E, ?, Set<E>>> {",
        "    public Collector<E, ?, Set<E>> get() {",
        "      return null;",
        "    }",
        "  }",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .compilesWithoutError();
  }

  @Test
  void validSetAutoMapper() {
    JavaFileObject javaFile = fromSource(
        "@CommandLineArguments",
        "abstract class Arguments {",
        "",
        "  @Parameter(shortName = 'x',",
        "             collectedBy = MyCollector.class)",
        "  abstract Set<BigInteger> bigIntegers();",
        "",
        "  static class MyCollector implements Supplier<Collector<BigInteger, ?, Set<BigInteger>>> {",
        "    public Collector<BigInteger, ?, Set<BigInteger>> get() {",
        "      return null;",
        "    }",
        "  }",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .compilesWithoutError();
  }

  @Test
  void validGenericCollector() {
    JavaFileObject javaFile = fromSource(
        "@CommandLineArguments",
        "abstract class Arguments {",
        "",
        "  @Parameter(shortName = 'x',",
        "             mappedBy = CustomBigIntegerMapper.class,",
        "             collectedBy = ToSetCollector.class)",
        "  abstract Set<BigInteger> bigSet();",
        "",
        "  static class CustomBigIntegerMapper implements Supplier<Function<String, BigInteger>> {",
        "    public Function<String, BigInteger> get() {",
        "      return s -> new BigInteger(s, 16);",
        "    }",
        "  }",
        "",
        "  static class ToSetCollector<E> implements Supplier<Collector<E, ?, Set<E>>> {",
        "    public Collector<E, ?, Set<E>> get() {",
        "      return Collectors.toSet();",
        "    }",
        "  }",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .compilesWithoutError();
  }

  @Test
  void validGenericCollectorAutoMapper() {
    JavaFileObject javaFile = fromSource(
        "@CommandLineArguments",
        "abstract class Arguments {",
        "",
        "  @Parameter(shortName = 'x',",
        "             collectedBy = ToSetCollector.class)",
        "  abstract Set<BigInteger> bigSet();",
        "",
        "  static class ToSetCollector<E> implements Supplier<Collector<E, ?, Set<E>>> {",
        "    public Collector<E, ?, Set<E>> get() {",
        "      return Collectors.toSet();",
        "    }",
        "  }",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .compilesWithoutError();
  }

  @Test
  void validMap() {
    JavaFileObject javaFile = fromSource(
        "@CommandLineArguments",
        "abstract class Arguments {",
        "",
        "  @Parameter(shortName = 'T',",
        "             mappedBy = MapEntryMapper.class,",
        "             collectedBy = MapEntryCollector.class)",
        "  abstract Map<String, LocalDate> map();",
        "",
        "  static class MapEntryMapper implements Supplier<Function<String, Map.Entry<String, LocalDate>>> {",
        "    public Function<String, Map.Entry<String, LocalDate>> get() {",
        "      return s -> {",
        "        String[] tokens = s.split(\":\", 2);",
        "        return new AbstractMap.SimpleImmutableEntry<>(tokens[0], LocalDate.parse(tokens[1]));",
        "      };",
        "    }",
        "  }",
        "",
        "  static class MapEntryCollector<K, V> implements Supplier<Collector<Map.Entry<K, V>, ?, Map<K, V>>> {",
        "    public Collector<Map.Entry<K, V>, ?, Map<K, V>> get() {",
        "      return Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue);",
        "    }",
        "  }",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .compilesWithoutError();
  }

  @Test
  void validBothMapperAndCollectorHaveTypeargs() {
    JavaFileObject javaFile = fromSource(
        "@CommandLineArguments",
        "abstract class Arguments {",
        "",
        "  @Parameter(shortName = 'x',",
        "             mappedBy = IdentityMapper.class,",
        "             collectedBy = ToListCollector.class)",
        "  abstract List<String> map();",
        "",
        "  static class IdentityMapper<M> implements Supplier<Function<M, M>> {",
        "    public Function<M, M> get() {",
        "      return null;",
        "    }",
        "  }",
        "",
        "  static class ToListCollector<E> implements Supplier<Collector<E, ?, List<E>>> {",
        "    public Collector<E, ?, List<E>> get() {",
        "      return null;",
        "    }",
        "  }",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .compilesWithoutError();
  }

  @Test
  void invalidOptionalAuto() {
    JavaFileObject javaFile = fromSource(
        "@CommandLineArguments",
        "abstract class Arguments {",
        "",
        "  @Parameter(shortName = 'x',",
        "             collectedBy = ToSetCollector.class)",
        "  abstract Set<Optional<Integer>> optionalIntegers();",
        "",
        "  static class ToSetCollector<E> implements Supplier<Collector<E, ?, Set<E>>> {",
        "    public Collector<E, ?, Set<E>> get() {",
        "      return Collectors.toSet();",
        "    }",
        "  }",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("Unknown parameter type.");
  }

  @Test
  void invalidBounds() {
    JavaFileObject javaFile = fromSource(
        "@CommandLineArguments",
        "abstract class Arguments {",
        "",
        "  @Parameter(shortName = 'x',",
        "             collectedBy = ToSetCollector.class)",
        "  abstract Set<Integer> integers();",
        "",
        "  static class ToSetCollector<E extends Long> implements Supplier<Collector<E, ?, Set<E>>> {",
        "    public Collector<E, ?, Set<E>> get() {",
        "      return Collectors.toSet();",
        "    }",
        "  }",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("There is a problem with the collector class: invalid bounds.");
  }

  @Test
  void invalidOptionalIntAuto() {
    JavaFileObject javaFile = fromSource(
        "@CommandLineArguments",
        "abstract class Arguments {",
        "",
        "  @Parameter(shortName = 'x',",
        "             collectedBy = ToSetCollector.class)",
        "  abstract Set<OptionalInt> optionalInts();",
        "",
        "  static class ToSetCollector<E> implements Supplier<Collector<E, ?, Set<E>>> {",
        "    public Collector<E, ?, Set<E>> get() {",
        "      return Collectors.toSet();",
        "    }",
        "  }",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("Unknown parameter type.");
  }

  @Test
  void invalidBoundSupplier() {
    JavaFileObject javaFile = fromSource(
        "@CommandLineArguments",
        "abstract class Arguments {",
        "",
        "  @Parameter(shortName = 'x',",
        "             collectedBy = A.class)",
        "  abstract Set<String> strings();",
        "",
        "  static class A implements ToSetCollector<Long> {",
        "    public Collector<Long, ?, Set<Long>> get() {",
        "      return Collectors.toSet();",
        "    }",
        "  }",
        "",
        "  interface ToSetCollector<E> extends Supplier<Collector<E, ?, Set<E>>> { }",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("The collector should return Set<String> but returns Set<Long>");
  }

  @Test
  void invalidBound() {
    JavaFileObject javaFile = fromSource(
        "@CommandLineArguments",
        "abstract class Arguments {",
        "",
        "  @Parameter(shortName = 'x',",
        "             collectedBy = A.class)",
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
        .withErrorContaining("The collector should return Set<String> but returns Set<Long>");
  }

  @Test
  void validBothMapperAndCollectorHaveTypeargsHard() {
    JavaFileObject javaFile = fromSource(
        "@CommandLineArguments",
        "abstract class Arguments {",
        "",
        "  @Parameter(shortName = 'x',",
        "             mappedBy = Map.class,",
        "             collectedBy = Collect.class)",
        "  abstract List<Integer> map();",
        "",
        "  static class Map<F extends java.lang.Number, E extends java.lang.CharSequence> implements Supplier<Function<E, F>> {",
        "    public Function<E, F> get() {",
        "      return null;",
        "    }",
        "  }",
        "",
        "  static class Collect<E extends java.lang.Number> implements Supplier<Collector<E, ?, List<E>>> {",
        "    public Collector<E, ?, List<E>> get() {",
        "      return null;",
        "    }",
        "  }",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .compilesWithoutError();
  }

  @Test
  void invalidBothMapperAndCollectorHaveTypeargsBadCollectorBounds() {
    JavaFileObject javaFile = fromSource(
        "@CommandLineArguments",
        "abstract class Arguments {",
        "",
        "  @Parameter(shortName = 'x',",
        "             mappedBy = Map.class,",
        "             collectedBy = Collect.class)",
        "  abstract List<Integer> map();",
        "",
        "  static class Map<F extends java.lang.Number, E extends java.lang.CharSequence> implements Supplier<Function<E, F>> {",
        "    public Function<E, F> get() {",
        "      return null;",
        "    }",
        "  }",
        "",
        "  static class Collect<E extends java.lang.Long> implements Supplier<Collector<E, ?, List<E>>> {",
        "    public Collector<E, ?, List<E>> get() {",
        "      return null;",
        "    }",
        "  }",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("There is a problem with the collector class: invalid bounds.");
  }

  // TODO inferring collector input from mapper output is currently not supported
  @Test
  void invalidBothMapperAndCollectorHaveTypeargsUnresolvedCollectorTypearg() {
    JavaFileObject javaFile = fromSource(
        "@CommandLineArguments",
        "abstract class Arguments {",
        "",
        "  @Parameter(shortName = 'x',",
        "             mappedBy = Map.class,",
        "             collectedBy = Collect.class)",
        "  abstract List<Integer> map();",
        "",
        "  static class Map<F extends java.lang.Number, E extends java.lang.CharSequence> implements Supplier<Function<E, F>> {",
        "    public Function<E, F> get() {",
        "      return null;",
        "    }",
        "  }",
        "",
        "  static class Collect<F, E extends java.lang.Number> implements Supplier<Collector<F, ?, List<E>>> {",
        "    public Collector<F, ?, List<E>> get() {",
        "      return null;",
        "    }",
        "  }",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("There is a problem with the collector class: could not resolve all type parameters.");
  }

  @Test
  void validBothMapperCollectorAndResultHaveTypeargs() {
    JavaFileObject javaFile = fromSource(
        "@CommandLineArguments",
        "abstract class Arguments {",
        "",
        "  @Parameter(shortName = 'x',",
        "             mappedBy = Map.class,",
        "             collectedBy = Collect.class)",
        "  abstract List<Result<String>> map();",
        "",
        "  static class Map<E, F extends java.util.Collection> implements Supplier<Function<E, F>> {",
        "    public Function<E, F> get() {",
        "      return null;",
        "    }",
        "  }",
        "",
        "  static class Collect<E extends Result> implements Supplier<Collector<Set<E>, ?, List<E>>> {",
        "    public Collector<Set<E>, ?, List<E>> get() {",
        "      return null;",
        "    }",
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
        "@CommandLineArguments",
        "abstract class Arguments {",
        "",
        "  @Parameter(shortName = 'x',",
        "             mappedBy = XMap.class,",
        "             collectedBy = YCol.class)",
        "  abstract List<String> map();",
        "",
        "  static class XMap<E> implements Supplier<Function<E, E>> {",
        "    public Function<E, E> get() {",
        "      return Function.identity();",
        "    }",
        "  }",
        "",
        "  static class YCol<E extends Integer> implements Supplier<Collector<E, ?, List<E>>> {",
        "    public Collector<E, ?, List<E>> get() {",
        "      return Collectors.toList();",
        "    }",
        "  }",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("There is a problem with the collector class: invalid bounds.");
  }


  @Test
  void bothMapperAndCollectorHaveTypeargsImpossibleFromString() {
    JavaFileObject javaFile = fromSource(
        "@CommandLineArguments",
        "abstract class Arguments {",
        "",
        "  @Parameter(shortName = 'x',",
        "             mappedBy = Identity.class,",
        "             collectedBy = Collect.class)",
        "  abstract List<Integer> ints();",
        "",
        "  static class Identity<E> implements Supplier<Function<E, E>> {",
        "    public Function<E, E> get() {",
        "      return null;",
        "    }",
        "  }",
        "",
        "  static class Collect<E> implements Supplier<Collector<E, ?, List<E>>> {",
        "    public Collector<E, ?, List<E>> get() {",
        "      return null;",
        "    }",
        "  }",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("There is a problem with the mapper class: invalid bounds");
  }

  @Test
  void bothMapperAndCollectorHaveTypeargsValid() {
    JavaFileObject javaFile = fromSource(
        "@CommandLineArguments",
        "abstract class Arguments {",
        "",
        "  @Parameter(shortName = 'x',",
        "             mappedBy = MakeList.class,",
        "             collectedBy = Concat.class)",
        "  abstract List<String> strings();",
        "",
        "  static class MakeList<E> implements Supplier<Function<E, List<E>>> {",
        "    public Function<E, List<E>> get() {",
        "      return null;",
        "    }",
        "  }",
        "",
        "  static class Concat<E> implements Supplier<Collector<List<E>, ?, List<E>>> {",
        "    public Collector<List<E>, ?, List<E>> get() {",
        "      return null;",
        "    }",
        "  }",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .compilesWithoutError();
  }

  @Test
  void validEnum() {
    JavaFileObject javaFile = fromSource(
        "@CommandLineArguments",
        "abstract class Arguments {",
        "",
        "  @Parameter(shortName = 'x', collectedBy = ToSetCollector.class)",
        "  abstract Set<Foo> foo();",
        "",
        "  enum Foo {",
        "    BAR",
        "   }",
        "",
        "  static class ToSetCollector<E> implements Supplier<Collector<E, ?, Set<E>>> {",
        "    public Collector<E, ?, Set<E>> get() {",
        "      return Collectors.toSet();",
        "    }",
        "  }",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .compilesWithoutError();
  }

  @Test
  void invalidEnum() {
    JavaFileObject javaFile = fromSource(
        "@CommandLineArguments",
        "abstract class Arguments {",
        "",
        "  @Parameter(shortName = 'x', collectedBy = ToSetCollector.class)",
        "  abstract Foo foo();",
        "",
        "  enum Foo {",
        "    BAR",
        "   }",
        "",
        "  static class ToSetCollector<E> implements Supplier<Collector<E, ?, Set<E>>> {",
        "    public Collector<E, ?, Set<E>> get() {",
        "      return Collectors.toSet();",
        "    }",
        "  }",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("The collector should return test.Arguments.Foo but returns Set<E>");
  }

  @Test
  void collectorInvalidNotCollector() {
    JavaFileObject javaFile = fromSource(
        "@CommandLineArguments",
        "abstract class Arguments {",
        "",
        "  @Parameter(shortName = 'x', collectedBy = ZapperSupplier.class)",
        "  abstract String zap();",
        "",
        "  interface ZapperSupplier extends Supplier<String> { }",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("not a Collector or Supplier<Collector>");
  }

  @Test
  void invalidFlagCollector() {
    JavaFileObject javaFile = fromSource(
        "@CommandLineArguments",
        "abstract class Arguments {",
        "",
        "  @Parameter(shortName = 'x',",
        "             flag = true,",
        "             collectedBy = MyCollector.class)",
        "  abstract Boolean myFlag();",
        "",
        "  static class MyCollector implements Supplier<Collector<String, ?, Boolean>> {",
        "    public Collector<String, ?, Boolean> get() {",
        "      return null;",
        "    }",
        "  }",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile();
  }
}
