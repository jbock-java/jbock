package net.jbock.processor;

import io.jbock.testing.compile.Compilation;
import org.junit.jupiter.api.Test;

import javax.tools.JavaFileObject;
import java.util.List;

import static io.jbock.testing.compile.CompilationSubject.assertThat;
import static net.jbock.processor.Processor.fromSource;

class ConverterTest {

    @Test
    void booleanConverter() {
        JavaFileObject javaFile = fromSource(
                "@Command",
                "abstract class Arguments {",
                "",
                "  @Parameter(converter = \"myConverter\")",
                "  abstract Boolean something();",
                "",
                "  static Either<String, Boolean> myConverter(String token) {",
                "    return null;",
                "  }",
                "}");
        List<String> expectedParser =
                List.of(
                        "  private static Arguments createImpl(ParseResult result) throws ExFailure {",
                        "    StandardConverters converters = new StandardConverters();",
                        "    Boolean something = result.param(0)",
                        "        .map(Arguments::myConverter)",
                        "        .orElseThrow(() -> new ExMissingItem(ItemType.PARAMETER, 0))",
                        "        .orElseThrow(failure -> new ExConvert(failure, ItemType.PARAMETER, 0));",
                        "    return new Arguments_Impl(something);",
                        "  }");
        Compilation compilation = Processor.compiler().compile(javaFile);
        assertThat(compilation).succeeded();
        assertThat(compilation).generatedSourceFile("test.ArgumentsParser")
                .containsLines(expectedParser);
    }
}
