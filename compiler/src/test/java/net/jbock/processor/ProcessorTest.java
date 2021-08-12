package net.jbock.processor;

import org.junit.jupiter.api.Test;

import javax.tools.JavaFileObject;

import static com.google.common.truth.Truth.assertAbout;
import static com.google.testing.compile.JavaSourcesSubjectFactory.javaSources;
import static java.util.Collections.singletonList;
import static net.jbock.processor.Processor.fromSource;

class ProcessorTest {

    @Test
    void missingDash() {
        JavaFileObject javaFile = fromSource(
                "@Command",
                "abstract class Arguments {",
                "  @Option(names = \"a\") abstract String a();",
                "}");
        assertAbout(javaSources()).that(singletonList(javaFile))
                .processedWith(Processor.testInstance())
                .failsToCompile()
                .withErrorContaining("invalid name: a");
    }

    @Test
    void emptyLongName() {
        JavaFileObject javaFile = fromSource(
                "@Command",
                "abstract class Arguments {",
                "  @Option(names = \"--\") abstract String a();",
                "}");
        assertAbout(javaSources()).that(singletonList(javaFile))
                .processedWith(Processor.testInstance())
                .failsToCompile()
                .withErrorContaining("invalid name: --");
    }

    @Test
    void badName() {
        JavaFileObject javaFile = fromSource(
                "@Command",
                "abstract class Arguments {",
                "",
                "  @Option(names = \"-\")",
                "  abstract boolean __();",
                "}");
        assertAbout(javaSources()).that(singletonList(javaFile))
                .processedWith(Processor.testInstance())
                .failsToCompile()
                .withErrorContaining("invalid name: -");
    }

    @Test
    void duplicateOptionName() {
        JavaFileObject javaFile = fromSource(
                "@Command",
                "abstract class Arguments {",
                "  @Option(names = \"--x\") abstract String a();",
                "  @Option(names = \"--x\") abstract String b();",
                "}");
        assertAbout(javaSources()).that(singletonList(javaFile))
                .processedWith(Processor.testInstance())
                .failsToCompile()
                .withErrorContaining("duplicate option name");
    }

    @Test
    void duplicateMnemonic() {
        JavaFileObject javaFile = fromSource(
                "@Command",
                "abstract class Arguments {",
                "  @Option(names = {\"--x\", \"-x\"}) abstract String a();",
                "  @Option(names = {\"--y\", \"-x\"}) abstract String b();",
                "}");
        assertAbout(javaSources()).that(singletonList(javaFile))
                .processedWith(Processor.testInstance())
                .failsToCompile()
                .withErrorContaining("duplicate option name: -x");
    }

    @Test
    void duplicateName() {
        JavaFileObject javaFile = fromSource(
                "@Command",
                "abstract class Arguments {",
                "  @Option(names = {\"--x\", \"--x\"}) abstract String a();",
                "}");
        assertAbout(javaSources()).that(singletonList(javaFile))
                .processedWith(Processor.testInstance())
                .failsToCompile()
                .withErrorContaining("duplicate option name: --x");
    }

    @Test
    void multiCharacterUnixOption() {
        JavaFileObject javaFile = fromSource(
                "@Command",
                "abstract class Arguments {",
                "  @Option(names = {\"-xx\"}) abstract String a();",
                "}");
        assertAbout(javaSources()).that(singletonList(javaFile))
                .processedWith(Processor.testInstance())
                .failsToCompile()
                .withErrorContaining("single-dash name must be single-character: -xx");
    }

    @Test
    void unknownReturnType() {
        JavaFileObject javaFile = fromSource(
                "@Command",
                "abstract class Arguments {",
                "",
                "  @Option(names = \"--x\")",
                "  abstract StringBuilder a();",
                "}");
        assertAbout(javaSources()).that(singletonList(javaFile))
                .processedWith(Processor.testInstance())
                .failsToCompile()
                .withErrorContaining("define a converter class that extends StringConverter<StringBuilder>" +
                        " or implements Supplier<StringConverter<StringBuilder>>");
    }

    @Test
    void runtimeException() {
        JavaFileObject javaFile = fromSource(
                "@Command",
                "abstract class Arguments {",
                "",
                "  @Option(names = \"--x\")",
                "  abstract String a() throws IllegalArgumentException;",
                "}");
        assertAbout(javaSources()).that(singletonList(javaFile))
                .processedWith(Processor.testInstance())
                .compilesWithoutError();
    }

    @Test
    void methodNotAbstract() {
        JavaFileObject javaFile = fromSource(
                "@Command",
                "class Arguments {",
                "",
                "  @Option(names = \"--x\")",
                "  String a() { return null; }",
                "}");
        assertAbout(javaSources()).that(singletonList(javaFile))
                .processedWith(Processor.testInstance())
                .failsToCompile()
                .withErrorContaining("missing method modifier: annotated method 'a' must be abstract");
    }

    @Test
    void rawList() {
        JavaFileObject javaFile = fromSource(
                "@Command",
                "abstract class Arguments {",
                "",
                "  @Option(names = \"--x\")",
                "  abstract List a();",
                "}");
        assertAbout(javaSources()).that(singletonList(javaFile))
                .processedWith(Processor.testInstance())
                .failsToCompile()
                .withErrorContaining("define a converter class that extends StringConverter<List>" +
                        " or implements Supplier<StringConverter<List>>");
    }

    @Test
    void rawOptional() {
        JavaFileObject javaFile = fromSource(
                "@Command",
                "abstract class Arguments {",
                "",
                "  @Option(names = \"--x\")",
                "  abstract Optional a();",
                "}");
        assertAbout(javaSources()).that(singletonList(javaFile))
                .processedWith(Processor.testInstance())
                .failsToCompile()
                .withErrorContaining("define a converter class that extends StringConverter<Optional>" +
                        " or implements Supplier<StringConverter<Optional>>");
    }

    @Test
    void parameterizedSet() {
        JavaFileObject javaFile = fromSource(
                "@Command",
                "abstract class Arguments {",
                "",
                "  @Option(names = \"--x\")",
                "  abstract java.util.Set<String> a();",
                "}");
        assertAbout(javaSources()).that(singletonList(javaFile))
                .processedWith(Processor.testInstance())
                .failsToCompile()
                .withErrorContaining("define a converter class that extends StringConverter<Set<String>>" +
                        " or implements Supplier<StringConverter<Set<String>>>");
    }

    @Test
    void checkedException() {
        JavaFileObject javaFile = fromSource(
                "@Command",
                "abstract class Arguments {",
                "",
                "  @Option(names = \"--x\")",
                "  abstract String a() throws java.io.IOException;",
                "}");
        assertAbout(javaSources()).that(singletonList(javaFile))
                .processedWith(Processor.testInstance())
                .failsToCompile()
                .withErrorContaining("invalid throws clause: found checked exception IOException");
    }

    @Test
    void inaccessibleException() {
        JavaFileObject javaFile = fromSource(
                "@Command",
                "abstract class Arguments {",
                "",
                "  @Option(names = \"--x\")",
                "  abstract String a() throws BlueMonday;",
                "",
                "  private static class BlueMonday extends RuntimeException {}",
                "}");
        assertAbout(javaSources()).that(singletonList(javaFile))
                .processedWith(Processor.testInstance())
                .failsToCompile()
                .withErrorContaining("invalid throws clause: declared exception BlueMonday is invalid: " +
                        "class 'BlueMonday may not be private");
    }

    @Test
    void uncheckedException() {
        JavaFileObject javaFile = fromSource(
                "@Command",
                "abstract class Arguments {",
                "",
                "  @Option(names = \"--x\")",
                "  abstract String a() throws IllegalArgumentException;",
                "}");
        assertAbout(javaSources()).that(singletonList(javaFile))
                .processedWith(Processor.testInstance())
                .compilesWithoutError();
    }

    @Test
    void constructorCheckedException() {
        JavaFileObject javaFile = fromSource(
                "@Command",
                "abstract class Arguments {",
                "",
                "  Arguments() throws java.io.IOException {}",
                "  @Option(names = \"--x\")",
                "  abstract String a();",
                "}");
        assertAbout(javaSources()).that(singletonList(javaFile))
                .processedWith(Processor.testInstance())
                .failsToCompile()
                .withErrorContaining("invalid throws clause: found checked exception IOException");
    }

    @Test
    void constructorUncheckedException() {
        JavaFileObject javaFile = fromSource(
                "@Command",
                "abstract class Arguments {",
                "",
                "  Arguments() throws IllegalArgumentException {}",
                "  @Option(names = \"--x\")",
                "  abstract String a();",
                "}");
        assertAbout(javaSources()).that(singletonList(javaFile))
                .processedWith(Processor.testInstance())
                .compilesWithoutError();
    }

    @Test
    void integerArray() {
        JavaFileObject javaFile = fromSource(
                "@Command",
                "abstract class Arguments {",
                "",
                "  @Option(names = \"--x\")",
                "  abstract int[] a();",
                "}");
        assertAbout(javaSources()).that(singletonList(javaFile))
                .processedWith(Processor.testInstance())
                .failsToCompile()
                .withErrorContaining("define a converter class that extends" +
                        " StringConverter<int[]> or implements Supplier<StringConverter<int[]>>");
    }

    @Test
    void utilDate() {
        JavaFileObject javaFile = fromSource(
                "@Command",
                "abstract class Arguments {",
                "",
                "  @Option(names = \"--x\")",
                "  abstract java.util.Date a();",
                "}");
        assertAbout(javaSources()).that(singletonList(javaFile))
                .processedWith(Processor.testInstance())
                .failsToCompile()
                .withErrorContaining("define a converter class that extends StringConverter<Date>" +
                        " or implements Supplier<StringConverter<Date>>");
    }

    @Test
    void voidType() {
        JavaFileObject javaFile = fromSource(
                "@Command",
                "abstract class Arguments {",
                "",
                "  @Option(names = \"--x\")",
                "  abstract void a();",
                "}");
        assertAbout(javaSources()).that(singletonList(javaFile))
                .processedWith(Processor.testInstance())
                .failsToCompile()
                .withErrorContaining("invalid return type: annotated method 'a' may not return VOID");
    }

    @Test
    void commandInterface() {
        JavaFileObject javaFile = fromSource(
                "@Command",
                "interface Arguments {",
                "  @Option(names = \"--a\")",
                "  abstract String a();",
                "}");
        assertAbout(javaSources()).that(singletonList(javaFile))
                .processedWith(Processor.testInstance())
                .compilesWithoutError();
    }

    @Test
    void whitespaceInName() {
        JavaFileObject javaFile = fromSource(
                "@Command",
                "abstract class Arguments {",
                "  @Option(names = \"--a \")",
                "  abstract String a();",
                "}");
        assertAbout(javaSources()).that(singletonList(javaFile))
                .processedWith(Processor.testInstance())
                .failsToCompile()
                .withErrorContaining("invalid name: whitespace characters: --a ");
    }

    @Test
    void noMethods() {
        JavaFileObject javaFile = fromSource(
                "@Command",
                "abstract class Arguments {",
                "}");
        assertAbout(javaSources()).that(singletonList(javaFile))
                .processedWith(Processor.testInstance())
                .compilesWithoutError();
    }

    @Test
    void oneOptionalIntNotOptional() {
        JavaFileObject javaFile = fromSource(
                "@Command",
                "abstract class Arguments {",
                "",
                "  @Option(names = \"--x\")",
                "  abstract java.util.OptionalInt b();",
                "}");
        assertAbout(javaSources()).that(singletonList(javaFile))
                .processedWith(Processor.testInstance())
                .compilesWithoutError();
    }

    @Test
    void threeNames() {
        JavaFileObject javaFile = fromSource(
                "@Command",
                "abstract class Arguments {",
                "",
                "  @Option(names = {\"--x\", \"-x\", \"-m\"})",
                "  abstract java.util.OptionalInt b();",
                "}");
        assertAbout(javaSources()).that(singletonList(javaFile))
                .processedWith(Processor.testInstance())
                .compilesWithoutError();
    }

    @Test
    void oneOptionalInt() {
        JavaFileObject javaFile = fromSource(
                "@Command",
                "abstract class Arguments {",
                "",
                "  @Option(names = \"--x\")",
                "  abstract java.util.OptionalInt b();",
                "}");
        assertAbout(javaSources()).that(singletonList(javaFile))
                .processedWith(Processor.testInstance())
                .compilesWithoutError();
    }

    @Test
    void simpleFlag() {
        JavaFileObject javaFile = fromSource(
                "@Command",
                "abstract class Arguments {",
                "",
                "  @Option(names = \"--x\")",
                "  abstract boolean x();",
                "}");
        assertAbout(javaSources()).that(singletonList(javaFile))
                .processedWith(Processor.testInstance())
                .compilesWithoutError();
    }

    @Test
    void clustering() {
        JavaFileObject javaFile = fromSource(
                "@Command",
                "abstract class Arguments {",
                "",
                "  @Option(names = \"-a\")",
                "  abstract boolean a();",
                "  @Option(names = \"-b\")",
                "  abstract boolean b();",
                "  @Option(names = \"-c\")",
                "  abstract List<String> c();",
                "}");
        assertAbout(javaSources()).that(singletonList(javaFile))
                .processedWith(Processor.testInstance())
                .compilesWithoutError();
    }

    @Test
    void reallyWeirdMethodName() {
        JavaFileObject javaFile = fromSource(
                "@Command",
                "abstract class Arguments {",
                "",
                "  @Option(names = \"--x\")",
                "  abstract boolean __();",
                "}");
        assertAbout(javaSources()).that(singletonList(javaFile))
                .processedWith(Processor.testInstance())
                .compilesWithoutError();
    }

    @Test
    void veryStrangeMethodName() {
        JavaFileObject javaFile = fromSource(
                "@Command",
                "abstract class Arguments {",
                "",
                "  @Option(names = \"--x\")",
                "  abstract boolean __9();",
                "}");
        assertAbout(javaSources()).that(singletonList(javaFile))
                .processedWith(Processor.testInstance())
                .compilesWithoutError();
    }

    @Test
    void fancyOptionNames() {
        JavaFileObject javaFile = fromSource(
                "@Command",
                "abstract class Arguments {",
                "",
                "  @Option(names = \"--Fancy\")",
                "  abstract boolean Fancy();",

                "  @Option(names = \"--fancy\")",
                "  abstract boolean fancy();",
                "}");
        assertAbout(javaSources()).that(singletonList(javaFile))
                .processedWith(Processor.testInstance())
                .compilesWithoutError();
    }

    @Test
    void badNameNoDash() {
        JavaFileObject javaFile = fromSource(
                "@Command",
                "abstract class Arguments {",
                "",
                "  @Option(names = \"ouch\")",
                "  abstract boolean f();",
                "}");
        assertAbout(javaSources()).that(singletonList(javaFile))
                .processedWith(Processor.testInstance())
                .failsToCompile()
                .withErrorContaining("invalid name: must start with a dash character: ouch");
    }

    @Test
    void badNameThreeDashes() {
        JavaFileObject javaFile = fromSource(
                "@Command",
                "abstract class Arguments {",
                "",
                "  @Option(names = \"---ouch\")",
                "  abstract boolean f();",
                "}");
        assertAbout(javaSources()).that(singletonList(javaFile))
                .processedWith(Processor.testInstance())
                .failsToCompile()
                .withErrorContaining("invalid name: cannot start with three dashes: ---ouch");
    }

    @Test
    void badNameEqualsSign() {
        JavaFileObject javaFile = fromSource(
                "@Command",
                "abstract class Arguments {",
                "",
                "  @Option(names = \"--e=mc2\")",
                "  abstract boolean f();",
                "}");
        assertAbout(javaSources()).that(singletonList(javaFile))
                .processedWith(Processor.testInstance())
                .failsToCompile()
                .withErrorContaining("invalid name: invalid character '=': --e=mc2");
    }

    @Test
    void badFlag() {
        JavaFileObject javaFile = fromSource(
                "@Command",
                "abstract class Arguments {",
                "",
                "  @Option(names = \"--x\")",
                "  abstract java.lang.Boolean x();",
                "}");
        assertAbout(javaSources()).that(singletonList(javaFile))
                .processedWith(Processor.testInstance())
                .failsToCompile()
                .withErrorContaining("define a converter class that extends StringConverter<Boolean>" +
                        " or implements Supplier<StringConverter<Boolean>>");
    }

    @Test
    void simpleInt() {
        JavaFileObject javaFile = fromSource(
                "@Command(description = \"y\", descriptionKey = \"y\")",
                "abstract class Arguments {",
                "",
                "  @Option(names = \"--x\", description = \"x\", descriptionKey = \"x\", paramLabel = \"x\")",
                "  abstract int aRequiredInt();",
                "}");
        assertAbout(javaSources()).that(singletonList(javaFile))
                .processedWith(Processor.testInstance())
                .compilesWithoutError();
    }

    @Test
    void parameterizedUnannotated() {
        JavaFileObject javaFile = fromSource(
                "@Command",
                "abstract class Arguments {",
                "",
                "  @Option(names = \"--xoxo\")",
                "  abstract int goodMethod();",
                "",
                "  abstract void parameterized(String foobar);",
                "}");
        assertAbout(javaSources()).that(singletonList(javaFile))
                .processedWith(Processor.testInstance())
                .failsToCompile()
                .withErrorContaining("missing annotation: add one of these annotations:" +
                        " [Option, Parameter, Parameters] to method 'parameterized'");
    }

    @Test
    void parameterizedAnnotated() {
        JavaFileObject javaFile = fromSource(
                "@Command",
                "abstract class Arguments {",
                "",
                "  @Option(names = \"--xoxo\")",
                "  abstract int goodMethod();",
                "",
                "  @Option(names = \"--xihuan\")",
                "  abstract void parameterized(String foobar);",
                "}");
        assertAbout(javaSources()).that(singletonList(javaFile))
                .processedWith(Processor.testInstance())
                .failsToCompile()
                .withErrorContaining("invalid method parameters: abstract method 'parameterized' may not have any" +
                        " parameters, but found: [foobar]");
    }

    @Test
    void noNames() {
        JavaFileObject javaFile = fromSource(
                "@Command",
                "abstract class Arguments {",
                "",
                "  @Option(names = {})",
                "  abstract int aRequiredInt();",
                "}");
        assertAbout(javaSources()).that(singletonList(javaFile))
                .processedWith(Processor.testInstance())
                .failsToCompile()
                .withErrorContaining("define at least one option name");
    }

    @Test
    void abstractMethodHasParameter() {
        JavaFileObject javaFile = fromSource(
                "@Command",
                "abstract class Arguments {",
                "  @Option(names = \"--x\") abstract String a(int b, int c);",
                "}");
        assertAbout(javaSources()).that(singletonList(javaFile))
                .processedWith(Processor.testInstance())
                .failsToCompile()
                .withErrorContaining("invalid method parameters: abstract method 'a' may not have any parameters, but found: [b, c]");
    }

    @Test
    void typeParameter() {
        JavaFileObject javaFile = fromSource(
                "@Command",
                "abstract class Arguments {",
                "  @Option(names = \"--x\") abstract <E> String a();",
                "}");
        assertAbout(javaSources()).that(singletonList(javaFile))
                .processedWith(Processor.testInstance())
                .failsToCompile()
                .withErrorContaining("invalid type parameters: annotated method 'a' may not have type parameters, but found: E");
    }

    @Test
    void typeParameters() {
        JavaFileObject javaFile = fromSource(
                "@Command",
                "abstract class Arguments {",
                "  @Option(names = \"--x\") abstract <E, F> String a();",
                "}");
        assertAbout(javaSources()).that(singletonList(javaFile))
                .processedWith(Processor.testInstance())
                .failsToCompile()
                .withErrorContaining("invalid type parameters: annotated method 'a' may not have type parameters, but found: E,F");
    }

    @Test
    void missingAnnotation() {
        JavaFileObject javaFile = fromSource(
                "@Command",
                "abstract class Arguments {",
                "",
                "  abstract List<String> a();",
                "}");
        assertAbout(javaSources()).that(singletonList(javaFile))
                .processedWith(Processor.testInstance())
                .failsToCompile()
                .withErrorContaining("missing annotation: add one of these annotations:" +
                        " [Option, Parameter, Parameters] to method 'a'");
    }

    @Test
    void positionalFlag() {
        JavaFileObject javaFile = fromSource(
                "@Command",
                "abstract class Arguments {",
                "",
                "  @Parameter(index = 0)",
                "  abstract boolean hello();",
                "}");
        assertAbout(javaSources()).that(singletonList(javaFile))
                .processedWith(Processor.testInstance())
                .failsToCompile()
                .withErrorContaining("define a converter class that extends StringConverter<Boolean>" +
                        " or implements Supplier<StringConverter<Boolean>>");
    }

    @Test
    void doubleAnnotation() {
        JavaFileObject javaFile = fromSource(
                "@Command",
                "abstract class Arguments {",
                "",
                "  @Option(names = \"--x\")",
                "  @Parameter(index = 0)",
                "  abstract List<String> a();",
                "}");
        assertAbout(javaSources()).that(singletonList(javaFile))
                .processedWith(Processor.testInstance())
                .failsToCompile()
                .withErrorContaining("annotate with either @Option or @Parameter but not both");
    }

    @Test
    void twoLists() {
        JavaFileObject javaFile = fromSource(
                "@Command",
                "abstract class Arguments {",
                "",
                "  @Option(names = \"--x\")",
                "  abstract List<String> a();",
                "",
                "  @Parameters",
                "  abstract List<String> b();",
                "}");
        assertAbout(javaSources()).that(singletonList(javaFile))
                .processedWith(Processor.testInstance())
                .compilesWithoutError();
    }

    @Test
    void innerEnum() {
        JavaFileObject javaFile = fromSource(
                "@Command",
                "abstract class Arguments {",
                "",
                "  @Option(names = \"--x\")",
                "  abstract Foo foo();",
                "",
                "  enum Foo {",
                "    BAR",
                "   }",
                "}");
        assertAbout(javaSources()).that(singletonList(javaFile))
                .processedWith(Processor.testInstance())
                .compilesWithoutError();
    }

    @Test
    void privateParameterType() {
        JavaFileObject javaFile = fromSource(
                "@Command",
                "abstract class Arguments {",
                "",
                "  @Option(names = \"--x\")",
                "  abstract Foo foo();",
                "",
                "  private enum Foo {",
                "    BAR",
                "   }",
                "}");
        assertAbout(javaSources()).that(singletonList(javaFile))
                .processedWith(Processor.testInstance())
                .failsToCompile()
                .withErrorContaining("inaccessible type: Foo");
    }

    @Test
    void unreachableParameterType() {
        JavaFileObject javaFile = fromSource(
                "@Command",
                "abstract class Arguments {",
                "",
                "  @Option(names = \"--x\")",
                "  abstract List<Foo> foo();",
                "",
                "  private enum Foo {",
                "    BAR",
                "   }",
                "}");
        assertAbout(javaSources()).that(singletonList(javaFile))
                .processedWith(Processor.testInstance())
                .failsToCompile()
                .withErrorContaining("inaccessible type: List<Foo>");
    }

    @Test
    void invalidNesting() {
        JavaFileObject javaFile = fromSource(
                "class Arguments {",
                "  private static class Foo {",
                "    @Command",
                "    abstract static class Bar {",
                "      @Option(names = \"--x\") abstract String a();",
                "    }",
                "  }",
                "}");
        assertAbout(javaSources()).that(singletonList(javaFile))
                .processedWith(Processor.testInstance())
                .failsToCompile()
                .withErrorContaining("invalid class: enclosing class 'Foo' may not be private");
    }

    @Test
    void invalidNestingInaccessible() {
        JavaFileObject javaFile = fromSource(
                "class Arguments {",
                "  class Foo {}",
                "  @Command",
                "  abstract static class Bar {",
                "    @Option(names = \"--x\") abstract Foo a();",
                "  }",
                "}");
        assertAbout(javaSources()).that(singletonList(javaFile))
                .processedWith(Processor.testInstance())
                .failsToCompile()
                .withErrorContaining("inaccessible type: Foo");
    }
}
