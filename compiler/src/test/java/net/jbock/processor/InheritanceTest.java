package net.jbock.processor;

import org.junit.jupiter.api.Test;

import javax.tools.JavaFileObject;

import static com.google.common.truth.Truth.assertAbout;
import static com.google.testing.compile.JavaSourcesSubjectFactory.javaSources;
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
        JavaFileObject javaFile = fromSource(
                "abstract class A {",
                "",
                "  abstract String inheritedMethod(String a);",
                "}",
                "abstract class B extends A {",
                "",
                "  String inheritedMethod(String a) { return null; }",
                "}",
                "@Command(superCommand = true)",
                "abstract class C extends B {",
                "",
                "  @Parameter(index = 0)",
                "  abstract String param();",
                "}");
        assertAbout(javaSources()).that(singletonList(javaFile))
                .processedWith(Processor.testInstance())
                .compilesWithoutError();
    }


    @Test
    void annotatedMethodOverridden() {
        JavaFileObject javaFile = fromSource(
                "abstract class A {",
                "",
                "  @Option(names = \"--ouch\")",
                "  abstract String wasp();",
                "}",
                "@Command",
                "abstract class B extends A {",
                "",
                "  String wasp() { return null; }",
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
                "@Command(superCommand = true)",
                "abstract class C extends B {",
                "",
                "  @Parameter(index = 0)",
                "  abstract String param();",
                "}");
        assertAbout(javaSources()).that(singletonList(javaFile))
                .processedWith(Processor.testInstance())
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
                "@Command(superCommand = true)",
                "abstract class C extends B {",
                "",
                "  @Parameter(index = 0)",
                "  abstract String param();",
                "}");
        assertAbout(javaSources()).that(singletonList(javaFile))
                .processedWith(Processor.testInstance())
                .failsToCompile()
                .withErrorContaining("add one of these annotations: @Option, @Parameter, @Parameters");
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
        JavaFileObject javaFile = fromSource(
                "interface A {",
                "",
                "  @Parameter(index = 0)",
                "  String param();",
                "}",
                "",
                "@Command",
                "abstract class B implements A {",
                "",
                "  @Option(names = \"-a\")",
                "  abstract String param();",
                "}");
        assertAbout(javaSources()).that(singletonList(javaFile))
                .processedWith(Processor.testInstance())
                .failsToCompile()
                .withErrorContaining("inheritance collision");
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
