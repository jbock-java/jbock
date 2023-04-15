package net.jbock.processor;

import org.junit.jupiter.api.Test;

import javax.tools.JavaFileObject;

import static io.jbock.common.truth.Truth.assertAbout;
import static io.jbock.testing.compile.JavaSourcesSubjectFactory.javaSources;
import static java.util.Collections.singletonList;
import static net.jbock.processor.Processor.fromSource;

class SuperCommandTest {

    @Test
    void cannotCombineCommandAndSuperCommand() {
        JavaFileObject javaFile = fromSource(
                "@Command",
                "@SuperCommand",
                "interface Arguments {",
                "",
                "  @Parameter(index = 0)",
                "  String a();",
                "}");
        assertAbout(javaSources()).that(singletonList(javaFile))
                .processedWith(Processor.testInstance())
                .failsToCompile()
                .withErrorContaining("not both");
    }
}
