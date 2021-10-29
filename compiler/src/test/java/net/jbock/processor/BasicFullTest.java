package net.jbock.processor;

import com.google.testing.compile.Compilation;
import org.junit.jupiter.api.Test;

import javax.tools.JavaFileObject;

import static com.google.testing.compile.CompilationSubject.assertThat;
import static com.google.testing.compile.JavaFileObjects.forSourceLines;
import static net.jbock.processor.Processor.fromSource;

class BasicFullTest {

    @Test
    public void testBasicGenerated() {
        JavaFileObject javaFile = fromSource(
                "@Command(skipGeneratingParseOrExitMethod = true)",
                "abstract class Arguments {",
                "",
                "  @Parameters",
                "  abstract List<String> hello();",
                "}");
        JavaFileObject generatedParser =
                forSourceLines(
                        "test.ArgumentsParser",
                        "",
                        "package test;",
                        "",
                        "import io.jbock.util.Either;",
                        "import io.jbock.util.Eithers;",
                        "import java.util.List;",
                        "import java.util.Map;",
                        "import java.util.function.Function;",
                        "import javax.annotation.processing.Generated;",
                        "import net.jbock.model.CommandModel;",
                        "import net.jbock.model.ItemType;",
                        "import net.jbock.model.Multiplicity;",
                        "import net.jbock.model.Parameter;",
                        "import net.jbock.parse.RestParser;",
                        "import net.jbock.util.ExConvert;",
                        "import net.jbock.util.ExFailure;",
                        "import net.jbock.util.ParsingFailed;",
                        "import net.jbock.util.StringConverter;",
                        "",
                        "@Generated(",
                        "  value = \"net.jbock.processor.JbockProcessor\",",
                        "  comments = \"https://github.com/jbock-java\")",
                        "final class ArgumentsParser {",
                        "",
                        "  Either<ParsingFailed, Arguments> parse(List<String> tokens) {",
                        "    RestParser<Void> parser = RestParser.create(Map.of(), Map.of(), 0);",
                        "    try {",
                        "      parser.parse(tokens);",
                        "      return Either.right(harvest(parser));",
                        "    } catch (ExFailure e) {",
                        "      return Either.left(e.toError(createModel()));",
                        "    }",
                        "  }",
                        "",
                        "  private Arguments harvest(RestParser<Void> parser) throws ExFailure {",
                        "    List<String> _hello = parser.rest()",
                        "          .map(StringConverter.create(Function.identity()))",
                        "          .collect(Eithers.toValidList())",
                        "          .orElseThrow(left -> new ExConvert(left, ItemType.PARAMETER, 0));",
                        "    return new Arguments_Impl(_hello);",
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
                        "}");
        JavaFileObject generatedImpl =
                forSourceLines(
                        "test.Arguments_Impl",
                        "",
                        "package test;",
                        "",
                        "import java.util.List;",
                        "import javax.annotation.processing.Generated;",
                        "",
                        "@Generated(",
                        "  value = \"net.jbock.processor.JbockProcessor\",",
                        "  comments = \"https://github.com/jbock-java\")",
                        "final class Arguments_Impl extends Arguments {",
                        "  private final List<String> hello;",
                        "",
                        "  Arguments_Impl(List<String> hello) {",
                        "    this.hello = hello;",
                        "  }",
                        "",
                        "  @Override",
                        "  List<String> hello() {",
                        "    return hello;",
                        "  }",
                        "}");
        Compilation compilation = Processor.compiler().compile(javaFile);
        assertThat(compilation).succeeded();
        assertThat(compilation)
                .generatedSourceFile("test.ArgumentsParser")
                .containsElementsIn(generatedParser);
        assertThat(compilation)
                .generatedSourceFile("test.Arguments_Impl")
                .containsElementsIn(generatedImpl);
    }

    @Test
    public void testPublicParser() {
        JavaFileObject javaFile = fromSource(
                "@Command(skipGeneratingParseOrExitMethod = true, publicParser = true)",
                "abstract class Arguments {",
                "",
                "  @Parameters",
                "  abstract List<String> hello();",
                "}");
        JavaFileObject generatedParser =
                forSourceLines(
                        "test.ArgumentsParser",
                        "",
                        "package test;",
                        "",
                        "import io.jbock.util.Either;",
                        "import io.jbock.util.Eithers;",
                        "import java.util.List;",
                        "import java.util.Map;",
                        "import java.util.function.Function;",
                        "import javax.annotation.processing.Generated;",
                        "import net.jbock.model.CommandModel;",
                        "import net.jbock.model.ItemType;",
                        "import net.jbock.model.Multiplicity;",
                        "import net.jbock.model.Parameter;",
                        "import net.jbock.parse.RestParser;",
                        "import net.jbock.util.ExConvert;",
                        "import net.jbock.util.ExFailure;",
                        "import net.jbock.util.ParsingFailed;",
                        "import net.jbock.util.StringConverter;",
                        "",
                        "@Generated(",
                        "  value = \"net.jbock.processor.JbockProcessor\",",
                        "  comments = \"https://github.com/jbock-java\")",
                        "public final class ArgumentsParser {",
                        "",
                        "  public Either<ParsingFailed, Arguments> parse(List<String> tokens) {",
                        "    RestParser<Void> parser = RestParser.create(Map.of(), Map.of(), 0);",
                        "    try {",
                        "      parser.parse(tokens);",
                        "      return Either.right(harvest(parser));",
                        "    } catch (ExFailure e) {",
                        "      return Either.left(e.toError(createModel()));",
                        "    }",
                        "  }",
                        "",
                        "  private Arguments harvest(RestParser<Void> parser) throws ExFailure {",
                        "    List<String> _hello = parser.rest()",
                        "          .map(StringConverter.create(Function.identity()))",
                        "          .collect(Eithers.toValidList())",
                        "          .orElseThrow(left -> new ExConvert(left, ItemType.PARAMETER, 0));",
                        "    return new Arguments_Impl(_hello);",
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
                        "}");
        JavaFileObject generatedImpl =
                forSourceLines(
                        "test.Arguments_Impl",
                        "",
                        "package test;",
                        "",
                        "import java.util.List;",
                        "import javax.annotation.processing.Generated;",
                        "",
                        "@Generated(",
                        "  value = \"net.jbock.processor.JbockProcessor\",",
                        "  comments = \"https://github.com/jbock-java\")",
                        "final class Arguments_Impl extends Arguments {",
                        "  private final List<String> hello;",
                        "",
                        "  Arguments_Impl(List<String> hello) {",
                        "    this.hello = hello;",
                        "  }",
                        "",
                        "  @Override",
                        "  List<String> hello() {",
                        "    return hello;",
                        "  }",
                        "}");
        Compilation compilation = Processor.compiler().compile(javaFile);
        assertThat(compilation).succeeded();
        assertThat(compilation)
                .generatedSourceFile("test.ArgumentsParser")
                .containsElementsIn(generatedParser);
        assertThat(compilation)
                .generatedSourceFile("test.Arguments_Impl")
                .containsElementsIn(generatedImpl);
    }
}
