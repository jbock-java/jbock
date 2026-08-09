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
                "  abstract Optional<String> a();",
                "",
                "  abstract List<String> rest();",
                "}");
        assertAbout(javaSources()).that(singletonList(javaFile))
                .processedWith(Processor.testInstance())
                .failsToCompile()
                .withErrorContaining("In a super command, parameters cannot be optional");
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
                .withErrorContaining("In a super command, a catch-all parameter must be defined");
    }

    // TODO move this to SuperCommandTest
    @Test
    void missingParamSuperCommand() {
        JavaFileObject javaFile = fromSource(
                "@Command(superCommand = true)",
                "interface Arguments {",
                "",
                "  @Option(names = \"--a\")",
                "  String a();",
                "",
                "  List<String> rest();",
                "}");
        assertAbout(javaSources()).that(singletonList(javaFile))
                .processedWith(Processor.testInstance())
                .failsToCompile()
                .withErrorContaining("In a super command, at least one parameter must be defined");
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
                .withErrorContaining("invalid position: expecting 1");
    }

    @Test
    void indexMissingZero() {
        JavaFileObject javaFile = fromSource(
                "@Command",
                "abstract class Arguments {",
                "  @Parameter(index = 1) abstract Optional<Integer> b();",
                "}");
        assertAbout(javaSources()).that(singletonList(javaFile))
                .processedWith(Processor.testInstance())
                .failsToCompile()
                .withErrorContaining("invalid position: expecting ");
    }

    @Test
    void positionalOptionalsNegative() {
        JavaFileObject javaFile = fromSource(
                "@Command",
                "abstract class Arguments {",
                "  @Parameter(index = -1) abstract Optional<String> a();",
                "}");
        assertAbout(javaSources()).that(singletonList(javaFile))
                .processedWith(Processor.testInstance())
                .failsToCompile()
                .withErrorContaining("invalid position: expecting 0 but found -1");
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
                .withErrorContaining("invalid position: expecting 0");
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
                .withErrorContaining("define a converter class that extends StringConverter<StringBuilder>" +
                        " or implements Supplier<StringConverter<StringBuilder>>");
    }

    @Test
    void positionalAllRanks() {
        JavaFileObject javaFile = fromSource(
                "@Command",
                "abstract class Arguments {",
                "  @Parameter(index = 0) abstract String b();",
                "  @Parameter(index = 1) abstract Optional<String> c();",
                "  @VarargsParameter abstract List<String> a();",
                "}");
        assertAbout(javaSources()).that(singletonList(javaFile))
                .processedWith(Processor.testInstance())
                .compilesWithoutError();
    }

    @Test
    void twoPositionalLists() {
        JavaFileObject javaFile = fromSource(
                "@Command",
                "abstract class Arguments {",
                "  @VarargsParameter abstract List<String> a();",
                "  @VarargsParameter abstract List<String> b();",
                "}");
        assertAbout(javaSources()).that(singletonList(javaFile))
                .processedWith(Processor.testInstance())
                .failsToCompile()
                .withErrorContaining("more than one catch-all parameter");
    }

    @Test
    void validList() {
        JavaFileObject javaFile = fromSource(
                "@Command",
                "abstract class Arguments {",
                "  @VarargsParameter abstract List<String> a();",
                "}");
        assertAbout(javaSources()).that(singletonList(javaFile))
                .processedWith(Processor.testInstance())
                .compilesWithoutError();
    }

    @Test
    void positionalOptionalBeforeRequired() {
        JavaFileObject javaFile = fromSource(
                "@Command",
                "abstract class Arguments {",
                "  @Parameter(index = 0) abstract Optional<String> a();",
                "  @Parameter(index = 1) abstract String b();",
                "}");
        assertAbout(javaSources()).that(singletonList(javaFile))
                .processedWith(Processor.testInstance())
                .failsToCompile()
                .withErrorContaining("position of required parameter 'b' is greater than position of optional parameter 'a'");
    }

    @Test
    void mayNotDeclareAsOptional() {
        JavaFileObject javaFile = fromSource(
                "@Command",
                "abstract class Arguments {",
                "  @Parameter(index = 0) abstract Optional<Integer> a();",
                "}");
        assertAbout(javaSources()).that(singletonList(javaFile))
                .processedWith(Processor.testInstance())
                .compilesWithoutError();
    }

    @Test
    void parametersInvalidNotList() {
        JavaFileObject javaFile = fromSource(
                "@Command",
                "abstract class Arguments {",
                "",
                "  @VarargsParameter",
                "  abstract Integer something();",
                "}");
        assertAbout(javaSources()).that(singletonList(javaFile))
                .processedWith(Processor.testInstance())
                .failsToCompile()
                .withErrorContaining("method 'something' is annotated with @VarargsParameter, so it must return java.util.List");
    }

    @Test
    void parametersInvalidNotListOptional() {
        JavaFileObject javaFile = fromSource(
                "@Command",
                "abstract class Arguments {",
                "",
                "  @VarargsParameter",
                "  abstract Optional<Integer> something();",
                "}");
        assertAbout(javaSources()).that(singletonList(javaFile))
                .processedWith(Processor.testInstance())
                .failsToCompile()
                .withErrorContaining("method 'something' is annotated with @VarargsParameter, so it must return java.util.List");
    }

    @Test
    void parameterInvalidList() {
        JavaFileObject javaFile = fromSource(
                "@Command",
                "abstract class Arguments {",
                "",
                "  @Parameter(index = 0)",
                "  abstract List<Integer> something();",
                "}");
        assertAbout(javaSources()).that(singletonList(javaFile))
                .processedWith(Processor.testInstance())
                .failsToCompile()
                .withErrorContaining("define a converter class that extends StringConverter<List<Integer>> or implements Supplier<StringConverter<List<Integer>>>");
    }
}
