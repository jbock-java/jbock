package net.jbock.compiler;

import org.junit.jupiter.api.Test;

import javax.tools.JavaFileObject;
import java.util.List;

import static com.google.common.truth.Truth.assertAbout;
import static com.google.testing.compile.JavaFileObjects.forSourceLines;
import static com.google.testing.compile.JavaSourcesSubjectFactory.javaSources;
import static java.util.Collections.singletonList;
import static net.jbock.compiler.ProcessorTest.withImports;

class CollectorTest {

  @Test
  void collectorValidExtendsCollector() {
    List<String> sourceLines = withImports(
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
    JavaFileObject javaFile = forSourceLines("test.Arguments", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .compilesWithoutError();
  }

  @Test
  void invalidNotRepeatable() {
    List<String> sourceLines = withImports(
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
    JavaFileObject javaFile = forSourceLines("test.Arguments", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .compilesWithoutError();
  }

  @Test
  void invalidCollectorClassDoesNotExist() {
    List<String> sourceLines = withImports(
        "@CommandLineArguments",
        "abstract class Arguments {",
        "",
        "  @Parameter(shortName = 'x',",
        "             collectedBy = JustNotExist.class)",
        "  abstract Set<String> strings();",
        "}");
    JavaFileObject javaFile = forSourceLines("test.Arguments", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("Invalid value of 'collectedBy'.");
  }

  @Test
  void validCollectorSupplier() {
    List<String> sourceLines = withImports(
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
    JavaFileObject javaFile = forSourceLines("test.Arguments", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .compilesWithoutError();
  }

  @Test
  void validCollector() {
    List<String> sourceLines = withImports(
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
    JavaFileObject javaFile = forSourceLines("test.Arguments", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .compilesWithoutError();
  }

  @Test
  void invalidReturnType() {
    List<String> sourceLines = withImports(
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
    JavaFileObject javaFile = forSourceLines("test.Arguments", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("collector");
  }

  @Test
  void invalidMapperMismatch() {
    List<String> sourceLines = withImports(
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
    JavaFileObject javaFile = forSourceLines("test.Arguments", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("problem");
  }

  @Test
  void validMapperMatch() {
    List<String> sourceLines = withImports(
        "@CommandLineArguments",
        "abstract class Arguments {",
        "",
        "  @Parameter(shortName = 'x',",
        "             mappedBy = HexMapper.class,",
        "             collectedBy = ToSetCollector.class)",
        "  abstract Set<String> bigIntegers();",
        "",
        "  static class HexMapper implements Supplier<Function<String, String>> {",
        "    public Function<String, String> get() {",
        "      return s -> s;",
        "    }",
        "  }",
        "",
        "  static class ToSetCollector<E> implements Supplier<Collector<E, ?, Set<E>>> {",
        "    public Collector<E, ?, Set<E>> get() {",
        "      return Collectors.toSet();",
        "    }",
        "  }",
        "}");
    JavaFileObject javaFile = forSourceLines("test.Arguments", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .compilesWithoutError();
  }

  @Test
  void validBigIntegers() {
    List<String> sourceLines = withImports(
        "@CommandLineArguments",
        "abstract class Arguments {",
        "",
        "  @Parameter(shortName = 'x',",
        "             mappedBy = HexMapper.class,",
        "             collectedBy = MyCollector.class)",
        "  abstract Set<BigInteger> bigIntegers();",
        "",
        "  static class HexMapper implements Supplier<Function<String, BigInteger>> {",
        "    public Function<String, BigInteger> get() {",
        "      return s -> new BigInteger(s, 16);",
        "    }",
        "  }",
        "",
        "  static class MyCollector implements Supplier<Collector<BigInteger, ?, Set<BigInteger>>> {",
        "    public Collector<BigInteger, ?, Set<BigInteger>> get() {",
        "      return Collectors.toSet();",
        "    }",
        "  }",
        "}");
    JavaFileObject javaFile = forSourceLines("test.Arguments", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .compilesWithoutError();
  }

  @Test
  void validGenericCollector() {
    List<String> sourceLines = withImports(
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
    JavaFileObject javaFile = forSourceLines("test.Arguments", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .compilesWithoutError();
  }

  @Test
  void validMap() {
    List<String> sourceLines = withImports(
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
    JavaFileObject javaFile = forSourceLines("test.Arguments", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .compilesWithoutError();
  }

  @Test
  void validBothMapperAndCollectorHaveTypeargs() {
    List<String> sourceLines = withImports(
        "@CommandLineArguments",
        "abstract class Arguments {",
        "",
        "  @Parameter(shortName = 'x',",
        "             mappedBy = XMap.class,",
        "             collectedBy = YCol.class)",
        "  abstract List<String> map();",
        "",
        "  static class XMap implements Supplier<Function<String, String>> {",
        "    public Function<String, String> get() {",
        "      return Function.identity();",
        "    }",
        "  }",
        "",
        "  static class YCol<E> implements Supplier<Collector<E, ?, List<E>>> {",
        "    public Collector<E, ?, List<E>> get() {",
        "      return Collectors.toList();",
        "    }",
        "  }",
        "}");
    JavaFileObject javaFile = forSourceLines("test.Arguments", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .compilesWithoutError();
  }

  @Test
  void invalidOptionalAuto() {
    List<String> sourceLines = withImports(
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
    JavaFileObject javaFile = forSourceLines("test.Arguments", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("Unknown parameter type. Define a custom mapper.");
  }

  @Test
  void invalidBounds() {
    List<String> sourceLines = withImports(
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
    JavaFileObject javaFile = forSourceLines("test.Arguments", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("There is a problem with the collector class: invalid bounds.");
  }

  @Test
  void invalidOptionalIntAuto() {
    List<String> sourceLines = withImports(
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
    JavaFileObject javaFile = forSourceLines("test.Arguments", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("Unknown parameter type. Define a custom mapper.");
  }

  @Test
  void invalidBoundSupplier() {
    List<String> sourceLines = withImports(
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
    JavaFileObject javaFile = forSourceLines("test.Arguments", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("The collector should return Set<String> but returns Set<Long>");
  }

  @Test
  void invalidBound() {
    List<String> sourceLines = withImports(
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
    JavaFileObject javaFile = forSourceLines("test.Arguments", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("The collector should return Set<String> but returns Set<Long>");
  }

  @Test
  void validBothMapperAndCollectorHaveTypeargsHard() {
    List<String> sourceLines = withImports(
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
    JavaFileObject javaFile = forSourceLines("test.Arguments", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .compilesWithoutError();
  }

  @Test
  void invalidBothMapperAndCollectorHaveTypeargsBadCollectorBounds() {
    List<String> sourceLines = withImports(
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
    JavaFileObject javaFile = forSourceLines("test.Arguments", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("There is a problem with the collector class: invalid bounds.");
  }

  // TODO inferring collector input from mapper output is currently not supported
  @Test
  void invalidBothMapperAndCollectorHaveTypeargsUnresolvedCollectorTypearg() {
    List<String> sourceLines = withImports(
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
    JavaFileObject javaFile = forSourceLines("test.Arguments", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("There is a problem with the collector class: could not resolve all type parameters.");
  }

  @Test
  void validBothMapperCollectorAndResultHaveTypeargs() {
    List<String> sourceLines = withImports(
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
    JavaFileObject javaFile = forSourceLines("test.Arguments", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .compilesWithoutError();
  }

  @Test
  void bothMapperAndCollectorHaveTypeargsInvalidBoundsOnCollector() {
    List<String> sourceLines = withImports(
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
    JavaFileObject javaFile = forSourceLines("test.Arguments", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("There is a problem with the collector class: invalid bounds.");
  }


  @Test
  void bothMapperAndCollectorHaveTypeargsImpossibleFromString() {
    List<String> sourceLines = withImports(
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
    JavaFileObject javaFile = forSourceLines("test.Arguments", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("There is a problem with the mapper class: could not infer type parameters.");
  }

  @Test
  void bothMapperAndCollectorHaveTypeargsValid() {
    List<String> sourceLines = withImports(
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
    JavaFileObject javaFile = forSourceLines("test.Arguments", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .compilesWithoutError();
  }

  @Test
  void validEnum() {
    List<String> sourceLines = withImports(
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
    JavaFileObject javaFile = forSourceLines("test.Arguments", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .compilesWithoutError();
  }

  @Test
  void invalidEnum() {
    List<String> sourceLines = withImports(
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
    JavaFileObject javaFile = forSourceLines("test.Arguments", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("The collector should return test.Arguments.Foo but returns Set<E>");
  }

  @Test
  void collectorInvalidNotCollector() {
    List<String> sourceLines = withImports(
        "@CommandLineArguments",
        "abstract class Arguments {",
        "",
        "  @Parameter(shortName = 'x', collectedBy = ZapperSupplier.class)",
        "  abstract String zap();",
        "",
        "  interface ZapperSupplier extends Supplier<String> { }",
        "}");
    JavaFileObject javaFile = forSourceLines("test.Arguments", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("There is a problem with the collector class: not a Collector or Supplier<Collector>.");
  }


  @Test
  void invalidFlagCollector() {
    List<String> sourceLines = withImports(
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
    JavaFileObject javaFile = forSourceLines("test.Arguments", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile();
  }
}
