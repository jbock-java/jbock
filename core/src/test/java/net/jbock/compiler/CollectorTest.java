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
        "  static class XMap<E> implements Supplier<Function<E, E>> {",
        "    public Function<E, E> get() {",
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
        .withErrorContaining("Define a mapper");
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
        .withErrorContaining("Define a mapper");
  }

  @Test
  void invalidBothMapperAndCollectorHaveTypeargs() {
    List<String> sourceLines = withImports(
        "@CommandLineArguments",
        "abstract class InvalidArguments {",
        "",
        "  @Parameter(shortName = 'x',",
        "             repeatable = true,",
        "             mappedBy = XMap.class,",
        "             collectedBy = YCol.class)",
        "  abstract List<String> map();",
        "",
        "  static class XMap<E extends Integer> implements Supplier<Function<E, E>> {",
        "    public Function<E, E> get() {",
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
    JavaFileObject javaFile = forSourceLines("test.InvalidArguments", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("mapper");
  }

  @Test
  void invalidBounds() {
    List<String> sourceLines = withImports(
        "@CommandLineArguments",
        "abstract class InvalidArguments {",
        "",
        "  @Parameter(shortName = 'x',",
        "             repeatable = true,",
        "             collectedBy = MyCollector.class)",
        "  abstract Set<String> strings();",
        "",
        "  static class MyCollector<E extends Integer> implements Supplier<Collector<E, ?, Set<E>>> {",
        "    public Collector<E, ?, Set<E>> get() {",
        "      return Collectors.toSet();",
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
        .withErrorContaining("Foo can't be unified with java.util.Set<E>");
  }
}
