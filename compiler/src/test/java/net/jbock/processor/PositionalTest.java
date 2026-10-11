package net.jbock.processor;

import org.junit.jupiter.api.Test;

import javax.tools.JavaFileObject;

import static io.jbock.common.truth.Truth.assertAbout;
import static io.jbock.testing.compile.JavaSourcesSubjectFactory.javaSources;
import static java.util.Collections.singletonList;
import static net.jbock.processor.Processor.fromSource;

class PositionalTest {

    @Test
    void simpleOptional() {
        JavaFileObject javaFile = fromSource(
                "@Command",
                "abstract class Arguments {",
                "",
                "  @Parameter(index = 0)",
                "  abstract Optional<String> a();",
                "}");
        assertAbout(javaSources()).that(singletonList(javaFile))
                .processedWith(Processor.testInstance())
                .compilesWithoutError();
    }

    @Test
    void superSimple() {
        JavaFileObject javaFile = fromSource(
                "@Command(superCommand = true, description = \"y\", descriptionKey = \"y\")",
                "interface Arguments {",
                "",
                "  @Parameter(index = 0, description = \"x\", descriptionKey = \"x\", paramLabel = \"x\")",
                "  abstract String a();",
                "",
                "  @Parameter",
                "  abstract List<String> rest();",
                "}");
        assertAbout(javaSources()).that(singletonList(javaFile))
                .processedWith(Processor.testInstance())
                .compilesWithoutError();
    }

    @Test
    void superSimpleOptional() {
        JavaFileObject javaFile = fromSource(
                "@Command(superCommand = true, description = \"y\", descriptionKey = \"y\")",
                "interface Arguments {",
                "",
                "  @Parameter(index = 0, description = \"x\", descriptionKey = \"x\", paramLabel = \"x\")",
                "  Optional<String> a();",
                "",
                "  @Parameter",
                "  List<String> rest();",
                "}");
        assertAbout(javaSources()).that(singletonList(javaFile))
                .processedWith(Processor.testInstance())
                .compilesWithoutError();
    }

    // TODO move this to SuperCommandTest
    @Test
    void superComplexOptional() {
        JavaFileObject javaFile = fromSource(
                "@Command(superCommand = true)",
                "interface Arguments {",
                "",
                "  @Parameter(index = 0)",
                "  String a();",
                "",
                "  @Option(names = \"--b\")",
                "  String b();",
                "",
                "  @Parameter",
                "  List<String> rest();",
                "}");
        assertAbout(javaSources()).that(singletonList(javaFile))
                .processedWith(Processor.testInstance())
                .compilesWithoutError();
    }

    // TODO move this to SuperCommandTest
    @Test
    void missingVarargsParameterInSuperCommand() {
        JavaFileObject javaFile = fromSource(
                "@Command(superCommand = true)",
                "interface Arguments {",
                "",
                "  @Parameter(index = 0)",
                "  String p();",
                "}");
        assertAbout(javaSources()).that(singletonList(javaFile))
                .processedWith(Processor.testInstance())
                .failsToCompile()
                .withErrorContaining("In a super command, a repeatable parameter must be defined");
    }

    @Test
    void missingParamSuperCommand() {
        JavaFileObject javaFile = fromSource(
                "@Command(superCommand = true)",
                "interface Arguments {",
                "",
                "  @Option(names = \"--a\")",
                "  String a();",
                "}");
        assertAbout(javaSources()).that(singletonList(javaFile))
                .processedWith(Processor.testInstance())
                .failsToCompile()
                .withErrorContaining("In a super command, a repeatable parameter must be defined");
    }

    @Test
    void duplicateDescriptionKeyParam() {
        JavaFileObject javaFile = fromSource(
                "@Command",
                "abstract class Arguments {",
                "",
                "  @Parameter(index = 0, descriptionKey = \"myKey\")",
                "  abstract String a();",
                "",
                "  @Option(names = \"--x\", descriptionKey = \"myKey\")",
                "  abstract String b();",
                "}");
        assertAbout(javaSources()).that(singletonList(javaFile))
                .processedWith(Processor.testInstance())
                .failsToCompile()
                .withErrorContaining("duplicate description key: myKey");
    }

    @Test
    void duplicateDescriptionKeyCommand() {
        JavaFileObject javaFile = fromSource(
                "@Command(descriptionKey = \"myKey\")",
                "abstract class Arguments {",
                "",
                "  @Parameter(index = 0, descriptionKey = \"myKey\")",
                "  abstract String a();",
                "}");
        assertAbout(javaSources()).that(singletonList(javaFile))
                .processedWith(Processor.testInstance())
                .failsToCompile()
                .withErrorContaining("duplicate description key: myKey");
    }

    @Test
    void indexGap() {
        JavaFileObject javaFile = fromSource(
                "@Command",
                "abstract class Arguments {",
                "  @Parameter(index = 0) abstract Optional<Integer> b();",
                "  @Parameter(index = 2) abstract Optional<String> c();",
                "}");
        assertAbout(javaSources()).that(singletonList(javaFile))
                .processedWith(Processor.testInstance())
                .failsToCompile()
                .withErrorContaining("invalid index: expecting 1");
    }

    @Test
    void indexMissingZero() {
        JavaFileObject javaFile = fromSource(
                "@Command",
                "interface Arguments {",
                "  @Parameter(index = 1) Optional<Integer> b();",
                "}");
        assertAbout(javaSources()).that(singletonList(javaFile))
                .processedWith(Processor.testInstance())
                .failsToCompile()
                .withErrorContaining("invalid index: expecting 0");
    }

    @Test
    void positionalOptionalsNegative() {
        JavaFileObject javaFile = fromSource(
                "@Command",
                "abstract class Arguments {",
                "  @Parameter(index = -1) abstract List<String> a();",
                "}");
        assertAbout(javaSources()).that(singletonList(javaFile))
                .processedWith(Processor.testInstance())
                .compilesWithoutError();
    }

    @Test
    void positionalConflict() {
        JavaFileObject javaFile = fromSource(
                "@Command",
                "abstract class Arguments {",
                "  @Parameter(index = 1) abstract int a();",
                "  @Parameter(index = 1) abstract int b();",
                "}");
        assertAbout(javaSources()).that(singletonList(javaFile))
                .processedWith(Processor.testInstance())
                .failsToCompile()
                .withErrorContaining("invalid index: expecting 0");
    }

    @Test
    void positionalBadReturnTypeStringBuilder() {
        JavaFileObject javaFile = fromSource(
                "@Command",
                "abstract class Arguments {",
                "  @Parameter(index = 0) abstract StringBuilder a();",
                "}");
        assertAbout(javaSources()).that(singletonList(javaFile))
                .processedWith(Processor.testInstance())
                .failsToCompile()
                .withErrorContaining("unknown type: StringBuilder");
    }

    @Test
    void positionalAllRanks() {
        JavaFileObject javaFile = fromSource(
                "@Command",
                "interface Arguments {",
                "  @Parameter(index = 0) String b();",
                "  @Parameter(index = 1) Optional<String> c();",
                "  @Parameter List<String> a();",
                "}");
        assertAbout(javaSources()).that(singletonList(javaFile))
                .processedWith(Processor.testInstance())
                .compilesWithoutError();
    }

    @Test
    void optionalBeforeRequired() {
        JavaFileObject javaFile = fromSource(
                "@Command",
                "interface Arguments {",
                "  @Parameter Optional<String> c();",
                "  @Parameter String b();",
                "}");
        assertAbout(javaSources()).that(singletonList(javaFile))
                .processedWith(Processor.testInstance())
                .failsToCompile()
                .withErrorContaining("position of required");
    }

    @Test
    void repeatableBeforeRequired() {
        JavaFileObject javaFile = fromSource(
                "@Command",
                "interface Arguments {",
                "  @Parameter List<String> c();",
                "  @Parameter String b();",
                "}");
        assertAbout(javaSources()).that(singletonList(javaFile))
                .processedWith(Processor.testInstance())
                .failsToCompile()
                .withErrorContaining("position of parameter");
    }

    @Test
    void repeatableBeforeOptional() {
        JavaFileObject javaFile = fromSource(
                "@Command",
                "interface Arguments {",
                "  @Parameter List<String> c();",
                "  @Parameter Optional<String> b();",
                "}");
        assertAbout(javaSources()).that(singletonList(javaFile))
                .processedWith(Processor.testInstance())
                .failsToCompile()
                .withErrorContaining("position of parameter");
    }

    @Test
    void moreThanOneCatchAll() {
        JavaFileObject javaFile = fromSource(
                "@Command",
                "interface Arguments {",
                "  @Parameter List<String> a();",
                "  @Parameter List<String> b();",
                "}");
        assertAbout(javaSources()).that(singletonList(javaFile))
                .processedWith(Processor.testInstance())
                .failsToCompile()
                .withErrorContaining("Only one parameter can be repeatable");
    }

    @Test
    void validList() {
        JavaFileObject javaFile = fromSource(
                "@Command",
                "interface Arguments {",
                "  @Parameter List<String> a();",
                "}");
        assertAbout(javaSources()).that(singletonList(javaFile))
                .processedWith(Processor.testInstance())
                .compilesWithoutError();
    }

    @Test
    void explicitPositionDoesNotAgree() {
        JavaFileObject javaFile = fromSource(
                "@Command",
                "interface Arguments {",
                "  @Parameter(index = 1) String a();",
                "  @Parameter(index = 0) String b();",
                "}");
        assertAbout(javaSources()).that(singletonList(javaFile))
                .processedWith(Processor.testInstance())
                .failsToCompile()
                .withErrorContaining("invalid index");
    }
}
