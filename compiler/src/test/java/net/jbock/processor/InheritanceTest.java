package net.jbock.processor;

import org.junit.jupiter.api.Test;

import javax.tools.JavaFileObject;
import java.util.List;

import static com.google.common.truth.Truth.assertAbout;
import static com.google.testing.compile.JavaSourcesSubjectFactory.javaSources;
import static java.util.Collections.singletonList;
import static net.jbock.processor.Processor.fromSource;

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
                .failsToCompile()
                .withErrorContaining("invalid superclass: expecting java.lang.Object, but found: test.Arguments");
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
                .failsToCompile()
                .withErrorContaining("missing command annotation: interface 'Arguments' must be annotated with net.jbock.Command");
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
        assertAbout(javaSources()).that(List.of(a, b, c))
                .processedWith(Processor.testInstance())
                .failsToCompile()
                .withErrorContaining("invalid superclass: expecting java.lang.Object, but found: test.B");
    }

    @Test
    void abstractOverrides() {
        JavaFileObject parent = fromSource(
                "interface Parent {",
                "",
                "  @Parameter(index = 0)",
                "  String source();",
                "",
                "  @Parameter(index = 1)",
                "  String dest();",
                "}");
        JavaFileObject c = fromSource(
                "@Command",
                "abstract class C implements Parent {",
                "",
                "  @Override",
                "  public abstract String dest();",
                "}");
        assertAbout(javaSources()).that(List.of(parent, c))
                .processedWith(Processor.testInstance())
                .failsToCompile()
                .withErrorContaining("invalid command class: the command class or interface may not implement or extend any interfaces, but found: [Parent]");
    }

    @Test
    void nonabstractOverrideNoAnnotationNeeded() {
        JavaFileObject parent = fromSource(
                "interface Parent {",
                "",
                "  boolean isSafe();",
                "}");
        JavaFileObject c = fromSource(
                "@Command",
                "abstract class C implements Parent {",
                "",
                "  @Override",
                "  public boolean isSafe() { return true; }",
                "}");
        assertAbout(javaSources()).that(List.of(parent, c))
                .processedWith(Processor.testInstance())
                .failsToCompile()
                .withErrorContaining("invalid command class: the command class or interface may not implement or extend any interfaces, but found: [Parent]");
    }

    @Test
    void annotatedMethodOverridden() {
        JavaFileObject a = fromSource(
                "@Command",
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
        assertAbout(javaSources()).that(List.of(a, b))
                .processedWith(Processor.testInstance())
                .failsToCompile()
                .withErrorContaining("invalid superclass: expecting java.lang.Object, but found: test.A");
    }

    @Test
    void siblingInterfaces() {
        JavaFileObject a = fromSource(
                "@Command",
                "interface Aaa {",
                "",
                "  @Option(names = \"--aaa\")",
                "  abstract String foo();",
                "}");
        JavaFileObject b = fromSource(
                "@Command",
                "interface Bbb {",
                "",
                "  @Option(names = \"--bbb\")",
                "  abstract String foo();",
                "}");
        JavaFileObject c = fromSource(
                "@Command",
                "abstract class C implements Bbb, Aaa {",
                "}");
        assertAbout(javaSources()).that(List.of(a, b, c))
                .processedWith(Processor.testInstance())
                .failsToCompile()
                .withErrorContaining("invalid command class: the command class or interface may not implement or extend any interfaces, but found: [Bbb, Aaa]");
    }

    @Test
    void annotatedMethodOverriddenAbstract() {
        JavaFileObject a = fromSource(
                "@Command",
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
        assertAbout(javaSources()).that(List.of(a, b))
                .processedWith(Processor.testInstance())
                .failsToCompile()
                .withErrorContaining("invalid superclass: expecting java.lang.Object, but found: test.A");
    }

    @Test
    void annotatedMethodOverriddenInSuperclass() {
        JavaFileObject a = fromSource(
                "@Command(superCommand = true)",
                "abstract class A {",
                "",
                "  @Parameter(index = 0)",
                "  abstract String inheritedMethod();",
                "}");
        JavaFileObject c = fromSource(
                "@Command(superCommand = true)",
                "abstract class C extends A {",
                "",
                "  @Parameter(index = 0)",
                "  abstract String param();",
                "}");
        assertAbout(javaSources()).that(List.of(a, c))
                .processedWith(Processor.testInstance())
                .failsToCompile()
                .withErrorContaining("invalid superclass: expecting java.lang.Object, but found: test.A");
    }
}
