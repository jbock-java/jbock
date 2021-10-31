package net.jbock.processor;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.JavaFileObjects;
import org.junit.jupiter.api.Test;

import javax.tools.JavaFileObject;

import static com.google.testing.compile.CompilationSubject.assertThat;
import static net.jbock.processor.Processor.fromSource;

class DependsOnGeneratedTypeTest {

    @Test
    void dependsOnGeneratedType() {
        JavaFileObject converter = JavaFileObjects.forSourceLines(
                "test.MapMap",
                "package test;",
                "",
                "import net.jbock.util.StringConverter;",
                "",
                "class MapMap extends StringConverter<GeneratedType> {",
                "",
                "  @Override",
                "  public GeneratedType convert(String token) { return null; }",
                "}");
        JavaFileObject command = fromSource(
                "@Command",
                "abstract class Arguments {",
                "",
                "  @Parameter(index = 0, converter = Mimi.class)",
                "  abstract Optional<GeneratedType> hello();",
                "",
                "  static class Mimi implements Supplier<StringConverter<GeneratedType>> {",
                "    public StringConverter<GeneratedType> get() { return new MapMap(); }",
                "  }",
                "}");

        GeneratingProcessor generatingProcessor = new GeneratingProcessor(
                "test.GeneratedType",
                "package test;",
                "",
                "final class GeneratedType {",
                "}");
        Compilation compilation =
                Processor.compiler(generatingProcessor)
                        .compile(converter, command);
        assertThat(compilation).succeededWithoutWarnings();
    }

}
