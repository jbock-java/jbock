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
}
