package net.jbock.compiler;

import static com.google.common.truth.Truth.assertAbout;
import static com.google.testing.compile.JavaFileObjects.forSourceLines;
import static com.google.testing.compile.JavaSourcesSubjectFactory.javaSources;
import static java.util.Collections.singletonList;

import java.util.Arrays;
import java.util.List;
import javax.tools.JavaFileObject;
import org.junit.Test;

public class ProcessorTest {

  @Test
  public void duplicateName() {
    List<String> sourceLines = Arrays.asList(
        "package test;",
        "import net.jbock.CommandLineArguments;",
        "import net.jbock.LongName;",
        "",
        "@CommandLineArguments",
        "abstract class InvalidArguments {",
        "  @LongName(\"x\") abstract String a();",
        "  @LongName(\"x\") abstract String b();",
        "}");
    JavaFileObject javaFile = forSourceLines("test.InvalidArguments", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("Duplicate longName: x");
  }

  @Test
  public void wrongType() {
    List<String> sourceLines = Arrays.asList(
        "package test;",
        "import net.jbock.CommandLineArguments;",
        "",
        "@CommandLineArguments",
        "abstract class InvalidArguments {",
        "  abstract int a();",
        "}");
    JavaFileObject javaFile = forSourceLines("test.InvalidArguments", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("a() returns int");
  }

  @Test
  public void privateException() {
    List<String> sourceLines = Arrays.asList(
        "package test;",
        "import net.jbock.CommandLineArguments;",
        "import net.jbock.LongName;",
        "",
        "@CommandLineArguments",
        "abstract class InvalidArguments {",
        "  abstract String a() throws IllegalArgumentException;",
        "}");
    JavaFileObject javaFile = forSourceLines("test.InvalidArguments", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("Class 'Hammer' may not be private");
  }

  @Test
  public void whitespace() {
    List<String> sourceLines = Arrays.asList(
        "package test;",
        "import net.jbock.CommandLineArguments;",
        "import net.jbock.LongName;",
        "import java.util.Optional;",
        "class JJob {",
        "  @CommandLineArguments JJob(@LongName(\"a b c\") Optional<String> a) {}",
        "}");
    JavaFileObject javaFile = forSourceLines("test.JJobParser", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("The name may not contain whitespace characters");
  }

  @Test
  public void booleanWrapper() {
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
        .withErrorContaining("Only Optional<String>, List<String> and boolean allowed, " +
            "but parameter a has type java.lang.Boolean");
  }

  @Test
  public void otherTokensAndEverythingAfter() {
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
        .withErrorContaining("@OtherTokens and @EverythingAfter cannot be on the same parameter");
  }

  @Test
  public void everythingAfterCollidesWithOption() {
    List<String> sourceLines = Arrays.asList(
        "package test;",
        "import net.jbock.CommandLineArguments;",
        "import net.jbock.EverythingAfter;",
        "import java.util.List;",
        "import java.util.Optional;",
        "class JJob {",
        "  @CommandLineArguments JJob(Optional<String> a, @EverythingAfter(\"--a\") List<String> b) {}",
        "}");
    JavaFileObject javaFile = forSourceLines("test.JJobParser", sourceLines);
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("@EverythingAfter coincides with a long option");
  }
}
