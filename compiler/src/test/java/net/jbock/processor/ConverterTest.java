package net.jbock.processor;

import org.junit.jupiter.api.Test;

import javax.tools.JavaFileObject;
import java.util.List;

import static io.jbock.common.truth.Truth.assertAbout;
import static io.jbock.testing.compile.JavaSourcesSubjectFactory.javaSources;
import static java.util.Collections.singletonList;
import static net.jbock.processor.Processor.fromSource;

class ConverterTest {

    @Test
    void parametersInvalidNotListEnum() {
        JavaFileObject myEnum = fromSource("enum MyEnum { A }");
        JavaFileObject javaFile = fromSource(
                "@Command",
                "abstract class Arguments {",
                "",
                "  @VarargsParameter",
                "  abstract MyEnum something();",
                "}");
        assertAbout(javaSources()).that(List.of(myEnum, javaFile))
                .processedWith(Processor.testInstance())
                .failsToCompile()
                .withErrorContaining("method 'something' is annotated with @VarargsParameter, so it must return java.util.List");
    }

    @Test
    void parametersInvalidNotList() {
        JavaFileObject javaFile = fromSource(
                "@Command",
                "abstract class Arguments {",
                "",
                "  @VarargsParameter(converter = MyConverter.class)",
                "  abstract Integer something();",
                "",
                "  static class MyConverter extends StringConverter<Integer> {",
                "    public Integer convert(String token) { return null; }",
                "  }",
                "}");
        assertAbout(javaSources()).that(singletonList(javaFile))
                .processedWith(Processor.testInstance())
                .failsToCompile()
                .withErrorContaining("method 'something' is annotated with @VarargsParameter, so it must return java.util.List");
    }

    @Test
    void parametersInvalidNotListEnumConverter() {
        JavaFileObject myEnum = fromSource("enum MyEnum { A }");
        JavaFileObject javaFile = fromSource(
                "@Command",
                "abstract class Arguments {",
                "",
                "  @VarargsParameter(converter = MyConverter.class)",
                "  abstract MyEnum something();",
                "",
                "  static class MyConverter extends StringConverter<MyEnum> {",
                "    public MyEnum convert(String token) { return null; }",
                "  }",
                "}");
        assertAbout(javaSources()).that(List.of(myEnum, javaFile))
                .processedWith(Processor.testInstance())
                .failsToCompile()
                .withErrorContaining("method 'something' is annotated with @VarargsParameter, so it must return java.util.List");
    }

    @Test
    void parametersInvalidNotListOptional() {
        JavaFileObject javaFile = fromSource(
                "@Command",
                "abstract class Arguments {",
                "",
                "  @VarargsParameter(converter = MyConverter.class)",
                "  abstract Optional<Integer> something();",
                "",
                "  static class MyConverter extends StringConverter<Integer> {",
                "    public Integer convert(String token) { return null; }",
                "  }",
                "}");
        assertAbout(javaSources()).that(singletonList(javaFile))
                .processedWith(Processor.testInstance())
                .failsToCompile()
                .withErrorContaining("method 'something' is annotated with @VarargsParameter, so it must return java.util.List");
    }

    @Test
    void parameterInvalidList() {
        JavaFileObject javaFile = fromSource(
                "@Command",
                "abstract class Arguments {",
                "",
                "  @Parameter(index = 0, converter = MyConverter.class)",
                "  abstract List<Integer> something();",
                "",
                "  static class MyConverter extends StringConverter<Integer> {",
                "    public Integer convert(String token) { return null; }",
                "  }",
                "}");
        assertAbout(javaSources()).that(singletonList(javaFile))
                .processedWith(Processor.testInstance())
                .failsToCompile()
                .withErrorContaining("a parameter may not be a list; drop the annotation or use @VarargsParameter");
    }

    @Test
    void validConvertToList() {
        JavaFileObject javaFile = fromSource(
                "@Command",
                "abstract class Arguments {",
                "",
                "  @Parameter(converter = MyConverter.class, index = 0)",
                "  abstract List<Integer> something();",
                "",
                "  static class MyConverter extends StringConverter<List<Integer>> {",
                "    public List<Integer> convert(String token) { return null; }",
                "  }",
                "}");
        assertAbout(javaSources()).that(singletonList(javaFile))
                .processedWith(Processor.testInstance())
                .failsToCompile()
                .withErrorContaining("a parameter may not be a list; drop the annotation or use @VarargsParameter");
    }
}
