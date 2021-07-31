package net.jbock.processor;

import org.junit.jupiter.api.Test;

import javax.tools.JavaFileObject;

import static com.google.common.truth.Truth.assertAbout;
import static com.google.testing.compile.JavaSourcesSubjectFactory.javaSources;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static net.jbock.processor.ProcessorTest.fromSource;

class InheritanceTest {

    @Test
    void simpleExtends() {
        JavaFileObject javaFile = fromSource(
                "abstract class Arguments {",
                "",
                "  @Parameter(index = 0)",
                "  abstract String something();",
                "",
                "  @Command(superCommand = true)",
                "  static abstract class Foo extends Arguments {",
                "  }",
                "}");
        assertAbout(javaSources()).that(singletonList(javaFile))
                .processedWith(Processor.testInstance())
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
                .processedWith(Processor.testInstance())
                .compilesWithoutError();
    }

    @Test
    void inheritedMethodIsImplemented() {
        JavaFileObject a = fromSource(
                "abstract class A {",
                "",
                "  abstract String inheritedMethod(String a);",
                "}");
        JavaFileObject b = fromSource(
                "abstract class B extends A {",
                "",
                "  String inheritedMethod(String a) { return null; }",
                "}");
        JavaFileObject c = fromSource(
                "@Command(superCommand = true)",
                "abstract class C extends B {",
                "",
                "  @Parameter(index = 0)",
                "  abstract String param();",
                "}");
        assertAbout(javaSources()).that(asList(a, b, c))
                .processedWith(Processor.testInstance())
                .compilesWithoutError();
    }


    @Test
    void annotatedMethodOverridden() {
        JavaFileObject a = fromSource(
                "abstract class A {",
                "",
                "  @Option(names = \"--ouch\")",
                "  abstract String wasp();",
                "}");
        JavaFileObject b = fromSource(
                "@Command",
                "abstract class B extends A {",
                "",
                "  String wasp() { return null; }",
                "",
                "  @Parameter(index = 0)",
                "  abstract String param();",
                "}");
        assertAbout(javaSources()).that(asList(a, b))
                .processedWith(Processor.testInstance())
                .failsToCompile()
                .withErrorContaining("annotated method is overridden");
    }

    @Test
    void annotatedMethodOverriddenAbstract() {
        JavaFileObject a = fromSource(
                "abstract class A {",
                "",
                "  @Option(names = \"--ouch\")",
                "  abstract String wasp();",
                "}");
        JavaFileObject b = fromSource(
                "@Command",
                "abstract class B extends A {",
                "",
                "  @Option(names = \"--ouch\")",
                "  abstract String wasp();",
                "",
                "  @Parameter(index = 0)",
                "  abstract String param();",
                "}");
        assertAbout(javaSources()).that(asList(a, b))
                .processedWith(Processor.testInstance())
                .failsToCompile()
                .withErrorContaining("annotated method is overridden");
    }

    @Test
    void inheritedMethodIsNotAnnotated() {
        JavaFileObject a = fromSource(
                "abstract class A {",
                "",
                "  abstract String inheritedMethod();",
                "}");
        JavaFileObject b = fromSource(
                "abstract class B extends A {",
                "}");
        JavaFileObject c = fromSource(
                "@Command(superCommand = true)",
                "abstract class C extends B {",
                "",
                "  @Parameter(index = 0)",
                "  abstract String param();",
                "}");
        assertAbout(javaSources()).that(asList(a, b, c))
                .processedWith(Processor.testInstance())
                .failsToCompile()
                .withErrorContaining("add one of these annotations: @Option, @Parameter, @Parameters");
    }

    @Test
    void annotatedMethodOverriddenInSuperclass() {
        JavaFileObject javaFile = fromSource(
                "abstract class A {",
                "",
                "  @Parameter(index = 1)",
                "  abstract String inheritedMethod();",
                "}",
                "abstract class B extends A {",
                "",
                "  String inheritedMethod() { return null; }",
                "}",
                "@Command(superCommand = true)",
                "abstract class C extends B {",
                "",
                "  @Parameter(index = 0)",
                "  abstract String param();",
                "}");
        assertAbout(javaSources()).that(singletonList(javaFile))
                .processedWith(Processor.testInstance())
                .failsToCompile()
                .withErrorContaining("annotated method is overridden");
    }

    @Test
    void annotatedMethodOverriddenInSuperclassAbstract() {
        JavaFileObject javaFile = fromSource(
                "abstract class A {",
                "",
                "  @Parameter(index = 1)",
                "  abstract String inheritedMethod();",
                "}",
                "abstract class B extends A {",
                "",
                "  abstract String inheritedMethod();",
                "}",
                "@Command(superCommand = true)",
                "abstract class C extends B {",
                "",
                "  @Parameter(index = 0)",
                "  abstract String param();",
                "}");
        assertAbout(javaSources()).that(singletonList(javaFile))
                .processedWith(Processor.testInstance())
                .failsToCompile()
                .withErrorContaining("annotated method is overridden");
    }

    @Test
    void parentParent() {
        JavaFileObject javaFile = fromSource(
                "interface ParentParent {",
                "",
                "  @Parameter(index = 0)",
                "  String source();",
                "}",
                "interface Parent extends ParentParent {",
                "}",
                "@Command",
                "abstract class C implements Parent, ParentParent {",
                "",
                "  @Parameter(index = 1)",
                "  abstract String dest();",
                "}");
        assertAbout(javaSources()).that(singletonList(javaFile))
                .processedWith(Processor.testInstance())
                .compilesWithoutError();
    }

    @Test
    void inheritanceCollision() {
        JavaFileObject a = fromSource(
                "interface A {",
                "",
                "  @Parameter(index = 0)",
                "  String param();",
                "}");
        JavaFileObject b = fromSource(
                "@Command",
                "abstract class B implements A {",
                "",
                "  @Option(names = \"-a\")",
                "  abstract String param();",
                "}");
        assertAbout(javaSources()).that(asList(a, b))
                .processedWith(Processor.testInstance())
                .failsToCompile()
                .withErrorContaining("annotated method is overridden");
    }

    @Test
    void inheritanceCollisionAbstract() {
        JavaFileObject a = fromSource(
                "interface A {",
                "",
                "  @Parameter(index = 0)",
                "  String param();",
                "}");
        JavaFileObject b = fromSource(
                "interface B extends A {",
                "",
                "  @Option(names = \"-a\")",
                "  String param();",
                "}");
        JavaFileObject c = fromSource(
                "@Command",
                "abstract class C implements B {",
                "}");
        assertAbout(javaSources()).that(asList(a, b, c))
                .processedWith(Processor.testInstance())
                .failsToCompile()
                .withErrorContaining("annotated method is overridden");
    }

    @Test
    void inheritedInterface() {
        JavaFileObject javaFile = fromSource(
                "interface I {}",
                "abstract class A implements I {}",
                "",
                "@Command(superCommand = true)",
                "abstract class B extends A {",
                "",
                "  @Parameter(index = 0)",
                "  abstract String param();",
                "}");
        assertAbout(javaSources()).that(singletonList(javaFile))
                .processedWith(Processor.testInstance())
                .compilesWithoutError();
    }
}
