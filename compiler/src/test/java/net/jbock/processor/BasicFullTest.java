package net.jbock.processor;

import io.jbock.testing.compile.Compilation;
import org.junit.jupiter.api.Test;

import javax.tools.JavaFileObject;
import java.util.List;

import static io.jbock.testing.compile.CompilationSubject.assertThat;
import static net.jbock.processor.Processor.fromSource;

class BasicFullTest {

    @Test
    void testBasicGenerated() {
        JavaFileObject javaFile = fromSource(
                "@Command(skipGeneratingParseOrExitMethod = true)",
                "abstract class Arguments {",
                "",
                "  @VarargsParameter",
                "  abstract List<String> hello();",
                "}");
        List<String> expectedParser =
                List.of(
                        "package test;",
                        "",
                        "import io.jbock.util.Either;",
                        "import io.jbock.util.Eithers;",
                        "import java.util.List;",
                        "import java.util.Map;",
                        "import javax.annotation.processing.Generated;",
                        "import net.jbock.contrib.StandardConverters;",
                        "import net.jbock.model.CommandModel;",
                        "import net.jbock.model.ItemType;",
                        "import net.jbock.model.Multiplicity;",
                        "import net.jbock.model.Parameter;",
                        "import net.jbock.parse.RestParser;",
                        "import net.jbock.util.ExConvert;",
                        "import net.jbock.util.ExFailure;",
                        "import net.jbock.util.ParsingFailed;",
                        "",
                        "@Generated(",
                        "    value = \"net.jbock.processor.JbockProcessor\",",
                        "    comments = \"https://github.com/jbock-java/jbock\"",
                        ")",
                        "final class ArgumentsParser {",
                        "  Either<ParsingFailed, Arguments> parse(List<String> tokens) {",
                        "    RestParser<Void> parser = RestParser.create(Map.of(), Map.of(), 0);",
                        "    try {",
                        "      parser.parse(tokens);",
                        "      return Either.right(new Arguments_Impl(parser));",
                        "    } catch (ExFailure e) {",
                        "      return Either.left(e.toError(createModel()));",
                        "    }",
                        "  }",
                        "",
                        "  CommandModel createModel() {",
                        "    return CommandModel.builder()",
                        "          .withProgramName(\"arguments\")",
                        "          .addParameter(Parameter.builder(Multiplicity.REPEATABLE)",
                        "            .withParamLabel(\"HELLO\")",
                        "            .build())",
                        "          .build();",
                        "  }",
                        "",
                        "  private static final class Arguments_Impl extends Arguments {",
                        "    final List<String> hello;",
                        "",
                        "    Arguments_Impl(ParseResult<Void> result) throws ExFailure {",
                        "      this.hello = result.rest()",
                        "            .map(StandardConverters.asString())",
                        "            .collect(Eithers.firstFailure())",
                        "            .orElseThrow(left -> new ExConvert(left, ItemType.PARAMETER, 0));",
                        "    }",
                        "",
                        "    @Override",
                        "    List<String> hello() {",
                        "      return hello;",
                        "    }",
                        "  }",
                        "}");
        Compilation compilation = Processor.compiler().compile(javaFile);
        assertThat(compilation).succeeded();
        assertThat(compilation).generatedSourceFile("test.ArgumentsParser")
                .containsLines(expectedParser);
    }

    @Test
    void testPublicParser() {
        JavaFileObject javaFile = fromSource(
                "@Command(skipGeneratingParseOrExitMethod = true, publicParser = true)",
                "abstract class Arguments {",
                "",
                "  @VarargsParameter",
                "  abstract List<String> hello();",
                "}");
        List<String> expectedParser =
                List.of(
                        "package test;",
                        "",
                        "import io.jbock.util.Either;",
                        "import io.jbock.util.Eithers;",
                        "import java.util.List;",
                        "import java.util.Map;",
                        "import javax.annotation.processing.Generated;",
                        "import net.jbock.contrib.StandardConverters;",
                        "import net.jbock.model.CommandModel;",
                        "import net.jbock.model.ItemType;",
                        "import net.jbock.model.Multiplicity;",
                        "import net.jbock.model.Parameter;",
                        "import net.jbock.parse.RestParser;",
                        "import net.jbock.util.ExConvert;",
                        "import net.jbock.util.ExFailure;",
                        "import net.jbock.util.ParsingFailed;",
                        "",
                        "@Generated(",
                        "    value = \"net.jbock.processor.JbockProcessor\",",
                        "    comments = \"https://github.com/jbock-java/jbock\"",
                        ")",
                        "public final class ArgumentsParser {",
                        "  public Either<ParsingFailed, Arguments> parse(List<String> tokens) {",
                        "    RestParser<Void> parser = RestParser.create(Map.of(), Map.of(), 0);",
                        "    try {",
                        "      parser.parse(tokens);",
                        "      return Either.right(new Arguments_Impl(parser));",
                        "    } catch (ExFailure e) {",
                        "      return Either.left(e.toError(createModel()));",
                        "    }",
                        "  }",
                        "",
                        "  public CommandModel createModel() {",
                        "    return CommandModel.builder()",
                        "          .withProgramName(\"arguments\")",
                        "          .addParameter(Parameter.builder(Multiplicity.REPEATABLE)",
                        "            .withParamLabel(\"HELLO\")",
                        "            .build())",
                        "          .build();",
                        "  }",
                        "",
                        "  private static final class Arguments_Impl extends Arguments {",
                        "    final List<String> hello;",
                        "",
                        "    Arguments_Impl(ParseResult<Void> result) throws ExFailure {",
                        "      this.hello = result.rest()",
                        "            .map(StandardConverters.asString())",
                        "            .collect(Eithers.firstFailure())",
                        "            .orElseThrow(left -> new ExConvert(left, ItemType.PARAMETER, 0));",
                        "    }",
                        "",
                        "    @Override",
                        "    List<String> hello() {",
                        "      return hello;",
                        "    }",
                        "  }",
                        "}");
        Compilation compilation = Processor.compiler().compile(javaFile);
        assertThat(compilation).succeeded();
        assertThat(compilation).generatedSourceFile("test.ArgumentsParser")
                .containsLines(expectedParser);
    }
}
