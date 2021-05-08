package net.jbock.compiler;

import org.junit.jupiter.api.Test;

import javax.tools.JavaFileObject;

import static com.google.common.truth.Truth.assertAbout;
import static com.google.testing.compile.JavaSourcesSubjectFactory.javaSources;
import static java.util.Collections.singletonList;
import static net.jbock.compiler.ProcessorTest.fromSource;

class InheritanceTest {

  @Test
  void simpleExtends() {
    JavaFileObject javaFile = fromSource(
        "abstract class Arguments {",
        "",
        "  @Parameter(index = 0)",
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
  void enclosedInInterface() {
    JavaFileObject javaFile = fromSource(
        "interface Arguments {",
        "",
        "  @Parameter(index = 0)",
        "  abstract String something();",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("use an abstract class, not an interface");
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
        "  @Parameter(index = 0)",
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
        "  @Parameter(index = 0)",
        "  abstract String param();",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("add one of these annotations: @Option, @Parameter, @Parameters");
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
        "  @Parameter(index = 0)",
        "  abstract String param();",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("add one of these annotations: @Option, @Parameter, @Parameters");
  }

  @Test
  void signatureMismatchType() {
    JavaFileObject javaFile = fromSource(
        "abstract class A {",
        "",
        "  abstract String inheritedMethod(int a);",
        "}",
        "abstract class B extends A {",
        "",
        "  String inheritedMethod(String a) { return null; }",
        "}",
        "@SuperCommand",
        "abstract class C extends B {",
        "",
        "  @Parameter(index = 0)",
        "  abstract String param();",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("add one of these annotations: @Option, @Parameter, @Parameters");
  }

  @Test
  void inheritedInterface() {
    JavaFileObject javaFile = fromSource(
        "interface I {}",
        "abstract class A implements I {}",
        "",
        "@SuperCommand",
        "abstract class B extends A {",
        "",
        "  @Parameter(index = 0)",
        "  abstract String param();",
        "}");
    assertAbout(javaSources()).that(singletonList(javaFile))
        .processedWith(new Processor())
        .failsToCompile()
        .withErrorContaining("this abstract class may not implement any interfaces");
  }
}
