package net.jbock.compiler;

import org.junit.Test;

import javax.tools.JavaFileObject;
import java.util.Arrays;
import java.util.List;

import static com.google.common.truth.Truth.assertAbout;
import static com.google.testing.compile.JavaFileObjects.forSourceLines;
import static com.google.testing.compile.JavaSourcesSubjectFactory.javaSources;
import static java.util.Collections.singletonList;

public class ProcessorTest {
  @Test
  public void process() throws Exception {
    List<String> sourceLines = Arrays.asList(
        "package test;",
        "import net.jbock.CommandLineArguments;",
        "import net.jbock.LongName;",
        "class JJob {",
        "  @CommandLineArguments JJob(@LongName(\"x\") String a, String b) {}",
        "}");
    JavaFileObject expected =
        forSourceLines("test.JJobParser",
            "package test;",
            "import javax.annotation.Generated;",
            "",
            "@Generated(value=\"net.jbock.compiler.Processor\")",
            "public final class JJobParser {",
            "  private JJobParser() {",
            "    throw new UnsupportedOperationException();",
            "  }",
            "}");
    JavaFileObject javaFile = forSourceLines("test.JJobParser", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .compilesWithoutError()
        .and().generatesSources(expected);
  }

}