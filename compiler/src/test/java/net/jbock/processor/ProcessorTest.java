package net.jbock.processor;

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
    void helpName() {
        JavaFileObject javaFile = fromSource(
                "@Command",
                "abstract class Arguments {",
                "  @Option(names = {\"--help\"}) abstract String a();",
                "}");
        assertAbout(javaSources()).that(singletonList(javaFile))
                .processedWith(Processor.testInstance())
                .failsToCompile()
                .withErrorContaining("'--help' is reserved, set 'helpEnabled=false' to allow it");
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
                .withErrorContaining("single-dash names must be single-character names: -xx");
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
                .withErrorContaining("define a converter that implements Function<String, StringBuilder>");
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
                .withErrorContaining("abstract method expected");
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
                .withErrorContaining("define a converter that implements Function<String, List>");
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
                .withErrorContaining("define a converter that implements Function<String, Optional>");
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
                .withErrorContaining("define a converter that implements Function<String, Set<String>>");
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
                .withErrorContaining("checked exceptions are not allowed here");
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
                .withErrorContaining("default constructor not found");
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
                .withErrorContaining("define a converter that implements Function<String, int[]>");
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
                .withErrorContaining("define a converter that implements Function<String, Date>");
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
                .withErrorContaining("name contains whitespace characters");
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
    void reallyStrangeMethodName() {
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
                .withErrorContaining("define a converter that implements Function<String, Boolean>");
    }

    @Test
    void simpleInt() {
        JavaFileObject javaFile = fromSource(
                "@Command(description = \"y\", descriptionKey = \"y\", atFileExpansion = false)",
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
                .withErrorContaining("empty argument list expected");
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
                .withErrorContaining("type parameter not expected here");
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
                .withErrorContaining("type parameters not expected here");
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
                .withErrorContaining("add one of these annotations: @Option, @Parameter, @Parameters");
    }

    @Test
    void positionalFlag() {
        JavaFileObject javaFile = fromSource(
                "@Command",
                "abstract class Arguments {",
                "",
                "  @Parameter(index = 1)",
                "  abstract boolean hello();",
                "}");
        assertAbout(javaSources()).that(singletonList(javaFile))
                .processedWith(Processor.testInstance())
                .failsToCompile()
                .withErrorContaining("define a converter that implements Function<String, Boolean>");
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
                .withErrorContaining("class cannot be private");
    }

    static JavaFileObject fromSource(String... lines) {
        List<String> sourceLines = withImports(lines);
        return forSourceLines("test.Arguments", sourceLines);
    }

    static List<String> withImports(String... lines) {
        List<String> header = Arrays.asList(
                "package test;",
                "",
                "import java.util.List;",
                "import java.util.Set;",
                "import java.util.Optional;",
                "import java.util.function.Function;",
                "import java.util.function.Supplier;",
                "import java.util.stream.Collector;",
                "",
                "import net.jbock.util.StringConverter;",
                "import net.jbock.Command;",
                "import net.jbock.Parameter;",
                "import net.jbock.Parameters;",
                "import net.jbock.Option;",
                "import net.jbock.Converter;",
                "");
        List<String> moreLines = new ArrayList<>(lines.length + header.size());
        moreLines.addAll(header);
        Collections.addAll(moreLines, lines);
        return moreLines;
    }
}
