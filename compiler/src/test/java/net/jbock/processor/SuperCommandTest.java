package net.jbock.processor;

import org.junit.jupiter.api.Test;

import javax.tools.JavaFileObject;

import static io.jbock.common.truth.Truth.assertAbout;
import static io.jbock.testing.compile.JavaSourcesSubjectFactory.javaSources;
import static java.util.Collections.singletonList;
import static net.jbock.processor.Processor.fromSource;

class SuperCommandTest {

    @Test
    void varargsParameterNotListOfStringInSuperCommand() {
        JavaFileObject javaFile = fromSource(
                "@Command(superCommand = true)",
                "interface Arguments {",
                "",
                "  @Parameter(index = 0)",
                "  String a();",
                "",
                "  @Parameter",
                "  List<Integer> rest();",
                "}");
        assertAbout(javaSources()).that(singletonList(javaFile))
                .processedWith(Processor.testInstance())
                .failsToCompile()
                .withErrorContaining("Only List<String> allowed in a super command");
    }

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
}
