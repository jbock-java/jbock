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
  void notRepeatable() {
    List<String> sourceLines = withImports(
        "@CommandLineArguments",
        "abstract class InvalidArguments {",
        "  @Parameter(collectedBy = MyCollector.class) abstract Set<String> strings();",
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
  void validCollector() {
    List<String> sourceLines = withImports(
        "@CommandLineArguments",
        "abstract class ValidArguments {",
        "",
        "  @Parameter(repeatable = true, collectedBy = MyCollector.class) abstract Set<String> strings();",
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
        "  @Parameter(repeatable = true, collectedBy = MyCollector.class) abstract Set<String> strings();",
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
  void validBigIntegers() {
    List<String> sourceLines = withImports(
        "@CommandLineArguments",
        "abstract class ValidArguments {",
        "",
        "  @Parameter(repeatable = true, mappedBy = HexMapper.class, collectedBy = MyCollector.class)",
        "  abstract Set<BigInteger> bigIntegers();",
        "",
        "  static class MyCollector implements Supplier<Collector<BigInteger, ?, Set<BigInteger>>> {",
        "    public Collector<BigInteger, ?, Set<BigInteger>> get() {",
        "      return Collectors.toSet();",
        "    }",
        "  }",
        "",
        "  static class HexMapper implements Supplier<Function<String, BigInteger>> {",
        "    public Function<String, BigInteger> get() {",
        "      return s -> new BigInteger(s, 16);",
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
        "  @Parameter(repeatable = true, mappedBy = CustomBigIntegerMapper.class, collectedBy = ToSetCollector.class)",
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
        "  @Parameter(repeatable = true, shortName = 'T', mappedBy = MapEntryMapper.class, collectedBy = MapEntryCollector.class)",
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
}
