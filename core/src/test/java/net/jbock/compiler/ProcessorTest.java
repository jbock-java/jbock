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
    JavaFileObject javaFile = forSourceLines("test.JJobParser", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .compilesWithoutError();
  }

  @Test
  public void duplicateName() throws Exception {
    List<String> sourceLines = Arrays.asList(
        "package test;",
        "import net.jbock.CommandLineArguments;",
        "import net.jbock.LongName;",
        "class JJob {",
        "  @CommandLineArguments JJob(@LongName(\"x\") String a, @LongName(\"x\") String b) {}",
        "}");
    JavaFileObject javaFile = forSourceLines("test.JJobParser", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("Duplicate longName: x");
  }

  @Test
  public void wrongType() throws Exception {
    List<String> sourceLines = Arrays.asList(
        "package test;",
        "import net.jbock.CommandLineArguments;",
        "import net.jbock.LongName;",
        "class JJob {",
        "  @CommandLineArguments JJob(@LongName(\"x\") int a) {}",
        "}");
    JavaFileObject javaFile = forSourceLines("test.JJobParser", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("Only String, boolean or java.util.List<String> allowed, " +
            "but parameter a has type int");
  }

  @Test
  public void whitespace() throws Exception {
    List<String> sourceLines = Arrays.asList(
        "package test;",
        "import net.jbock.CommandLineArguments;",
        "import net.jbock.LongName;",
        "class JJob {",
        "  @CommandLineArguments JJob(@LongName(\"a b c\") String a) {}",
        "}");
    JavaFileObject javaFile = forSourceLines("test.JJobParser", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("The name may not contain whitespace characters");
  }

  @Test
  public void booleanWrapper() throws Exception {
    List<String> sourceLines = Arrays.asList(
        "package test;",
        "import net.jbock.CommandLineArguments;",
        "import net.jbock.LongName;",
        "class JJob {",
        "  @CommandLineArguments JJob(@LongName(\"a\") Boolean a) {}",
        "}");
    JavaFileObject javaFile = forSourceLines("test.JJobParser", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("Only String, boolean or java.util.List<String> allowed, but parameter a " +
            "has type java.lang.Boolean");
  }

  @Test
  public void badCombination() throws Exception {
    List<String> sourceLines = Arrays.asList(
        "package test;",
        "import net.jbock.CommandLineArguments;",
        "import net.jbock.EverythingAfter;",
        "import net.jbock.OtherTokens;",
        "import java.util.List;",
        "class JJob {",
        "  @CommandLineArguments JJob(@OtherTokens @EverythingAfter(\"--\") List<String> a) {}",
        "}");
    JavaFileObject javaFile = forSourceLines("test.JJobParser", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("One argument may not have both @OtherTokens and @EverythingAfter");
  }
}