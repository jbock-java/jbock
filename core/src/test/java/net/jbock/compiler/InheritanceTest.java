package net.jbock.compiler;

import org.junit.jupiter.api.Test;

import javax.tools.JavaFileObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.google.common.truth.Truth.assertAbout;
import static com.google.testing.compile.JavaFileObjects.forSourceLines;
import static com.google.testing.compile.JavaSourcesSubjectFactory.javaSources;
import static java.util.Collections.singletonList;
import static net.jbock.compiler.ProcessorTest.fromSource;

class InheritanceTest {

  @Test
  void simpleExtends() {
    JavaFileObject javaFile = fromSource(
        "abstract class Arguments {",
        "",
        "  @Param(0)",
        "  abstract String something();",
        "",
        "  @SuperCommand",
        "  static abstract class Foo extends Arguments {",
        "  }",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .compilesWithoutError();
  }

  @Test
  void inheritedMethodIsImplemented() {
    JavaFileObject javaFile = fromSource(
        "abstract class A {",
        "",
        "  abstract String inheritedMethod(String a);",
        "}",
        "abstract class B extends A {",
        "",
        "  String inheritedMethod(String a) { return null; }",
        "}",
        "@SuperCommand",
        "abstract class C extends B {",
        "",
        "  @Param(0)",
        "  abstract String param();",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .compilesWithoutError();
  }

  @Test
  void inheritedMethodIsNotAnnotated() {
    JavaFileObject javaFile = fromSource(
        "abstract class A {",
        "",
        "  abstract String inheritedMethod();",
        "}",
        "",
        "abstract class B extends A {",
        "}",
        "",
        "@SuperCommand",
        "abstract class C extends B {",
        "",
        "  @Param(0)",
        "  abstract String param();",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("add @Option or @Param annotation");
  }

  @Test
  void signatureMismatchArity() {
    JavaFileObject javaFile = fromSource(
        "abstract class A {",
        "",
        "  abstract String inheritedMethod();",
        "}",
        "abstract class B extends A {",
        "",
        "  String inheritedMethod(String a) { return null; }",
        "}",
        "@SuperCommand",
        "abstract class C extends B {",
        "",
        "  @Param(0)",
        "  abstract String param();",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("add @Option or @Param annotation");
  }
}
