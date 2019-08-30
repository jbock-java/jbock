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
  void invalidNotRepeatable() {
    List<String> sourceLines = withImports(
        "@CommandLineArguments",
        "abstract class InvalidArguments {",
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
    JavaFileObject javaFile = forSourceLines("test.InvalidArguments", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("must be declared repeatable");
  }

  @Test
  void invalidCollectorClassDoesntExist() {
    List<String> sourceLines = withImports(
        "@CommandLineArguments",
        "abstract class InvalidArguments {",
        "",
        "  @Parameter(shortName = 'x',",
        "             repeatable = true,",
        "             collectedBy = JustNotExist.class)",
        "  abstract Set<String> strings();",
        "}");
    JavaFileObject javaFile = forSourceLines("test.InvalidArguments", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("Invalid value of 'collectedBy'.");
  }

  @Test
  void validCollectorSupplier() {
    List<String> sourceLines = withImports(
        "@CommandLineArguments",
        "abstract class ValidArguments {",
        "",
        "  @Parameter(shortName = 'x',",
        "             repeatable = true,",
        "             collectedBy = MyCollector.class)",
        "  abstract Set<String> strings();",
        "",
        "  static class MyCollector implements Supplier<Collector<String, ?, Set<String>>> {",
        "    public Collector<String, ?, Set<String>> get() {",
        "      return Collectors.toSet();",
        "    }",
        "  }",
        "}");
    JavaFileObject javaFile = forSourceLines("test.ValidArguments", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .compilesWithoutError();
  }

  @Test
  void validCollector() {
    List<String> sourceLines = withImports(
        "@CommandLineArguments",
        "abstract class ValidArguments {",
        "",
        "  @Parameter(shortName = 'x',",
        "             repeatable = true,",
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
    JavaFileObject javaFile = forSourceLines("test.ValidArguments", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .compilesWithoutError();
  }

  @Test
  void invalidReturnType() {
    List<String> sourceLines = withImports(
        "@CommandLineArguments",
        "abstract class InvalidArguments {",
        "",
        "  @Parameter(shortName = 'x',",
        "             repeatable = true,",
        "             collectedBy = MyCollector.class)",
        "  abstract Set<String> strings();",
        "",
        "  static class MyCollector implements Supplier<Collector<String, ?, List<String>>> {",
        "    public Collector<String, ?, List<String>> get() {",
        "      return Collectors.toList();",
        "    }",
        "  }",
        "}");
    JavaFileObject javaFile = forSourceLines("test.InvalidArguments", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("collector");
  }

  @Test
  void invalidMapperMismatch() {
    List<String> sourceLines = withImports(
        "@CommandLineArguments",
        "abstract class InvalidArguments {",
        "",
        "  @Parameter(shortName = 'x',",
        "             repeatable = true,",
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
    JavaFileObject javaFile = forSourceLines("test.InvalidArguments", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("problem");
  }

  @Test
  void validMapperMatch() {
    List<String> sourceLines = withImports(
        "@CommandLineArguments",
        "abstract class ValidArguments {",
        "",
        "  @Parameter(shortName = 'x',",
        "             repeatable = true,",
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
    JavaFileObject javaFile = forSourceLines("test.ValidArguments", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .compilesWithoutError();
  }

  @Test
  void validBigIntegers() {
    List<String> sourceLines = withImports(
        "@CommandLineArguments",
        "abstract class ValidArguments {",
        "",
        "  @Parameter(shortName = 'x',",
        "             repeatable = true,",
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
    JavaFileObject javaFile = forSourceLines("test.ValidArguments", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .compilesWithoutError();
  }

  @Test
  void validGenericCollector() {
    List<String> sourceLines = withImports(
        "@CommandLineArguments",
        "abstract class ValidArguments {",
        "",
        "  @Parameter(shortName = 'x',",
        "             repeatable = true,",
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
    JavaFileObject javaFile = forSourceLines("test.ValidArguments", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .compilesWithoutError();
  }

  @Test
  void validMap() {
    List<String> sourceLines = withImports(
        "@CommandLineArguments",
        "abstract class ValidArguments {",
        "",
        "  @Parameter(shortName = 'T',",
        "             repeatable = true,",
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
    JavaFileObject javaFile = forSourceLines("test.ValidArguments", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .compilesWithoutError();
  }

  @Test
  void validBothMapperAndCollectorHaveTypeargs() {
    List<String> sourceLines = withImports(
        "@CommandLineArguments",
        "abstract class ValidArguments {",
        "",
        "  @Parameter(shortName = 'x',",
        "             repeatable = true,",
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
    JavaFileObject javaFile = forSourceLines("test.ValidArguments", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .compilesWithoutError();
  }

  @Test
  void invalidOptionalAuto() {
    List<String> sourceLines = withImports(
        "@CommandLineArguments",
        "abstract class InvalidArguments {",
        "",
        "  @Parameter(shortName = 'x',",
        "             repeatable = true,",
        "             collectedBy = ToSetCollector.class)",
        "  abstract Set<Optional<Integer>> optionalIntegers();",
        "",
        "  static class ToSetCollector<E> implements Supplier<Collector<E, ?, Set<E>>> {",
        "    public Collector<E, ?, Set<E>> get() {",
        "      return Collectors.toSet();",
        "    }",
        "  }",
        "}");
    JavaFileObject javaFile = forSourceLines("test.InvalidArguments", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("Unknown parameter type. Define a custom mapper.");
  }

  @Test
  void invalidBounds() {
    List<String> sourceLines = withImports(
        "@CommandLineArguments",
        "abstract class InvalidArguments {",
        "",
        "  @Parameter(shortName = 'x',",
        "             repeatable = true,",
        "             collectedBy = ToSetCollector.class)",
        "  abstract Set<Integer> integers();",
        "",
        "  static class ToSetCollector<E extends Long> implements Supplier<Collector<E, ?, Set<E>>> {",
        "    public Collector<E, ?, Set<E>> get() {",
        "      return Collectors.toSet();",
        "    }",
        "  }",
        "}");
    JavaFileObject javaFile = forSourceLines("test.InvalidArguments", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("There is a problem with the collector class: Invalid bounds.");
  }

  @Test
  void invalidOptionalIntAuto() {
    List<String> sourceLines = withImports(
        "@CommandLineArguments",
        "abstract class InvalidArguments {",
        "",
        "  @Parameter(shortName = 'x',",
        "             repeatable = true,",
        "             collectedBy = ToSetCollector.class)",
        "  abstract Set<OptionalInt> optionalInts();",
        "",
        "  static class ToSetCollector<E> implements Supplier<Collector<E, ?, Set<E>>> {",
        "    public Collector<E, ?, Set<E>> get() {",
        "      return Collectors.toSet();",
        "    }",
        "  }",
        "}");
    JavaFileObject javaFile = forSourceLines("test.InvalidArguments", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("Unknown parameter type. Define a custom mapper.");
  }

  @Test
  void invalidBoundSupplier() {
    List<String> sourceLines = withImports(
        "@CommandLineArguments",
        "abstract class InvalidArguments {",
        "",
        "  @Parameter(shortName = 'x',",
        "             repeatable = true,",
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
    JavaFileObject javaFile = forSourceLines("test.InvalidArguments", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("The collector should return Set<String> but returns Set<Long>");
  }

  @Test
  void invalidBound() {
    List<String> sourceLines = withImports(
        "@CommandLineArguments",
        "abstract class InvalidArguments {",
        "",
        "  @Parameter(shortName = 'x',",
        "             repeatable = true,",
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
    JavaFileObject javaFile = forSourceLines("test.InvalidArguments", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor(true))
        .failsToCompile()
        .withErrorContaining("The collector should return Set<String> but returns Set<Long>");
  }

  @Test
  void validBothMapperAndCollectorHaveTypeargsHard() {
    List<String> sourceLines = withImports(
        "@CommandLineArguments",
        "abstract class ValidArguments {",
        "",
        "  @Parameter(shortName = 'x',",
        "             repeatable = true,",
        "             mappedBy = XMap.class,",
        "             collectedBy = YCol.class)",
        "  abstract List<Integer> map();",
        "",
        "  static class XMap<F extends java.lang.Number, E extends java.lang.CharSequence> implements Supplier<Function<E, F>> {",
        "    public Function<E, F> get() {",
        "      return null;",
        "    }",
        "  }",
        "",
        "  static class YCol<E> implements Supplier<Collector<E, ?, List<E>>> {",
        "    public Collector<E, ?, List<E>> get() {",
        "      return Collectors.toList();",
        "    }",
        "  }",
        "}");
    JavaFileObject javaFile = forSourceLines("test.ValidArguments", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .compilesWithoutError();
  }

  @Test
  void bothMapperAndCollectorHaveTypeargsInvalidBoundsOnCollector() {
    List<String> sourceLines = withImports(
        "@CommandLineArguments",
        "abstract class ValidArguments {",
        "",
        "  @Parameter(shortName = 'x',",
        "             repeatable = true,",
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
    JavaFileObject javaFile = forSourceLines("test.ValidArguments", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("There is a problem with the collector class: Invalid bounds.");
  }

  @Test
  void validEnum() {
    List<String> sourceLines = withImports(
        "@CommandLineArguments",
        "abstract class ValidArguments {",
        "",
        "  @Parameter(repeatable = true, shortName = 'x', collectedBy = ToSetCollector.class)",
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
    JavaFileObject javaFile = forSourceLines("test.ValidArguments", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .compilesWithoutError();
  }

  @Test
  void invalidEnum() {
    List<String> sourceLines = withImports(
        "@CommandLineArguments",
        "abstract class InvalidArguments {",
        "",
        "  @Parameter(repeatable = true, shortName = 'x', collectedBy = ToSetCollector.class)",
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
    JavaFileObject javaFile = forSourceLines("test.InvalidArguments", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("The collector should return test.InvalidArguments.Foo but returns Set<E>");
  }

  @Test
  void collectorInvalidNotCollector() {
    List<String> sourceLines = withImports(
        "@CommandLineArguments",
        "abstract class InvalidArguments {",
        "",
        "  @Parameter(repeatable = true, shortName = 'x', collectedBy = ZapperSupplier.class)",
        "  abstract String zap();",
        "",
        "  interface ZapperSupplier extends Supplier<String> { }",
        "}");
    JavaFileObject javaFile = forSourceLines("test.InvalidArguments", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("There is a problem with the collector class: must either implement Collector or Supplier<Collector>");
  }


  @Test
  void invalidFlagCollector() {
    List<String> sourceLines = withImports(
        "@CommandLineArguments",
        "abstract class InvalidArguments {",
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
    JavaFileObject javaFile = forSourceLines("test.InvalidArguments", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile();
  }
}
