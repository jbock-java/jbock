package net.jbock.compiler;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import javax.tools.JavaFileObject;

import static com.google.common.truth.Truth.assertAbout;
import static com.google.testing.compile.JavaSourcesSubjectFactory.javaSources;
import static java.util.Collections.singletonList;
import static net.jbock.compiler.ProcessorTest.fromSource;

@Disabled("Inferring left to right (mapper to collector) is not supported")
public class PreferenceTest {

  @Test
  void invalidBothMapperAndCollectorHaveTypeargsBadCollectorBounds() {
    JavaFileObject javaFile = fromSource(
        "@Command",
        "abstract class Arguments {",
        "",
        "  @Option(names = \"--x\",",
        "          converter = Map.class,",
        "          collectedBy = Collect.class)",
        "  abstract List<Integer> map();",
        "",
        "  static class Map<A extends CharSequence, B extends Number> implements Supplier<Function<A, B>> {",
        "    public Function<A, B> get() {",
        "      return null;",
        "    }",
        "  }",
        "",
        "  static class Collect<E extends Long> implements Supplier<Collector<E, ?, List<E>>> {",
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
  void validFreeTypevarsInMapperAndCollectorMapperPreferencePossibleIntegerToNumber() {
    JavaFileObject javaFile = fromSource(
        "@Command",
        "abstract class Arguments {",
        "",
        "  @Option(names = \"--x\",",
        "          converter = Map.class,",
        "          collectedBy = Collect.class)",
        "  abstract List<Integer> map();",
        "",
        "  static class Map<A , B extends Integer> implements Supplier<Function<A, List<B>>> {",
        "    public Function<A, List<B>> get() {",
        "      return null;",
        "    }",
        "  }",
        "",
        "  static class Collect<F extends Number, E> implements Supplier<Collector<List<F>, ?, List<E>>> {",
        "    public Collector<List<F>, ?, List<E>> get() {",
        "      return null;",
        "    }",
        "  }",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .compilesWithoutError();
  }


  @Test
  void validFreeTypevarsInMapperAndCollectorMapperPreference() {
    JavaFileObject javaFile = fromSource(
        "@Command",
        "abstract class Arguments {",
        "",
        "  @Option(names = \"--x\",",
        "          converter = Map.class,",
        "          collectedBy = Collect.class)",
        "  abstract List<Integer> map();",
        "",
        "  static class Map<A extends CharSequence, B extends Number> implements Supplier<Function<A, B>> {",
        "    public Function<A, B> get() {",
        "      return null;",
        "    }",
        "  }",
        "",
        "  static class Collect<E, F> implements Supplier<Collector<E, ?, List<F>>> {",
        "    public Collector<E, ?, List<F>> get() {",
        "      return null;",
        "    }",
        "  }",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .compilesWithoutError();
  }


  @Test
  void bothMapperAndCollectorHaveTypeargsValid() {
    JavaFileObject javaFile = fromSource(
        "@Command",
        "abstract class Arguments {",
        "",
        "  @Option(names = \"--x\",",
        "          converter = MakeList.class,",
        "          collectedBy = Concat.class)",
        "  abstract List<String> strings();",
        "",
        "  static class MakeList<A extends Number> implements Supplier<Function<String, List<A>>> {",
        "    public Function<String, List<A>> get() {",
        "      return null;",
        "    }",
        "  }",
        "",
        "  static class Concat<E, F> implements Supplier<Collector<List<E>, ?, List<F>>> {",
        "    public Collector<List<E>, ?, List<F>> get() {",
        "      return null;",
        "    }",
        "  }",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .compilesWithoutError();
  }
}
