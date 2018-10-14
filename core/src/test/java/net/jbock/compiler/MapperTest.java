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
}
