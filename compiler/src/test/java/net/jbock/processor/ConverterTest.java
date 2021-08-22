package net.jbock.processor;

import org.junit.jupiter.api.Test;

import javax.tools.JavaFileObject;
import java.util.List;

import static com.google.common.truth.Truth.assertAbout;
import static com.google.testing.compile.JavaFileObjects.forSourceLines;
import static com.google.testing.compile.JavaSourcesSubjectFactory.javaSources;
import static java.util.Collections.singletonList;
import static net.jbock.processor.Processor.fromSource;

class ConverterTest {

    @Test
    void converterNotAnInnerClass() {
        JavaFileObject converter = fromSource(
                "class MapMap extends StringConverter<String> {",
                "",
                "  @Override",
                "  public String convert(String token) { return null; }",
                "}");
        JavaFileObject javaFile = fromSource(
                "@Command",
                "abstract class Arguments {",
                "",
                "  @Option(names = \"--x\", converter = Mimi.class)",
                "  abstract String foo();",
                "",
                "  static class Mimi implements Supplier<StringConverter<String>> {",
                "    public StringConverter<String> get() { return new MapMap(); }",
                "  }",
                "}");
        assertAbout(javaSources()).that(List.of(converter, javaFile))
                .processedWith(Processor.testInstance())
                .compilesWithoutError();
    }

    @Test
    void converterImplementsBothFunctionAndSupplier() {
        JavaFileObject javaFile = fromSource(
                "@Command",
                "abstract class Arguments {",
                "",
                "  @Option(names = \"--x\", converter = MapMap.class)",
                "  abstract String foo();",
                "",
                "  static class MapMap extends StringConverter<String> implements Supplier<StringConverter<String>> {",
                "    public String convert(String token) { return null; }",
                "    public StringConverter<String> get() { return null; }",
                "  }",
                "}");
        assertAbout(javaSources()).that(List.of(javaFile))
                .processedWith(Processor.testInstance())
                .compilesWithoutError();
    }

    @Test
    void doesNotExtendStringConverter() {
        JavaFileObject javaFile = fromSource(
                "@Command",
                "abstract class Arguments {",
                "",
                "  static class MapMap {}",
                "",
                "  @Option(names = \"--x\", converter = MapMap.class)",
                "  abstract String foo();",
                "",
                "}");
        assertAbout(javaSources()).that(List.of(javaFile))
                .processedWith(Processor.testInstance())
                .failsToCompile()
                .withErrorContaining("invalid converter class: converter must extend StringConverter<X> or implement Supplier<StringConverter<X>>");
    }

    @Test
    void missingConverterAnnotation() {
        JavaFileObject converter = forSourceLines(
                "something.MapMap",
                "package something;",
                "public class MapMap extends net.jbock.util.StringConverter<String> {",
                "  public String convert(String token) { return null; }",
                "}");
        JavaFileObject javaFile = fromSource(
                "@Command",
                "abstract class Arguments {",
                "",
                "  @Option(names = \"--x\", converter = something.MapMap.class)",
                "  abstract String foo();",
                "}");
        assertAbout(javaSources()).that(List.of(converter, javaFile))
                .processedWith(Processor.testInstance())
                .failsToCompile()
                .withErrorContaining("invalid converter class: converter of 'foo' must be an inner class of " +
                        "the command class 'Arguments'");
    }

    @Test
    void abstractConverter() {
        JavaFileObject javaFile = fromSource(
                "@Command",
                "abstract class Arguments {",
                "",
                "  @Option(names = \"--x\", converter = MapMap.class)",
                "  abstract String foo();",
                "",
                "  static abstract class MapMap extends StringConverter<String> {",
                "    public String convert(String token) { return null; }",
                "  }",
                "}");
        assertAbout(javaSources()).that(List.of(javaFile))
                .processedWith(Processor.testInstance())
                .failsToCompile()
                .withErrorContaining("invalid converter class: the converter class 'MapMap' may not be abstract");
    }

    @Test
    void validArrayMapperSupplier() {
        JavaFileObject javaFile = fromSource(
                "@Command",
                "abstract class Arguments {",
                "",
                "  @Option(names = \"--x\", converter = ArrayMapper.class)",
                "  abstract Optional<int[]> foo();",
                "",
                "  static class ArrayMapper implements Supplier<StringConverter<int[]>> {",
                "    public StringConverter<int[]> get() { return null; }",
                "  }",
                "}");
        assertAbout(javaSources()).that(singletonList(javaFile))
                .processedWith(Processor.testInstance())
                .compilesWithoutError();
    }

    @Test
    void validArrayMapper() {
        JavaFileObject javaFile = fromSource(
                "@Command",
                "abstract class Arguments {",
                "",
                "  @Option(names = \"--x\", converter = ArrayMapper.class)",
                "  abstract Optional<int[]> foo();",
                "",
                "  static class ArrayMapper extends StringConverter<int[]> {",
                "    public int[] convert(String s) { return null; }",
                "  }",
                "}");
        assertAbout(javaSources()).that(singletonList(javaFile))
                .processedWith(Processor.testInstance())
                .compilesWithoutError();
    }

    @Test
    void validBooleanList() {
        JavaFileObject javaFile = fromSource(
                "@Command",
                "abstract class Arguments {",
                "",
                "  @Parameters(converter = BooleanMapper.class)",
                "  abstract List<Boolean> booleanList();",
                "",
                "  static class BooleanMapper implements Supplier<StringConverter<Boolean>> {",
                "    public StringConverter<Boolean> get() { return null; }",
                "  }",
                "}");
        assertAbout(javaSources()).that(singletonList(javaFile))
                .processedWith(Processor.testInstance())
                .compilesWithoutError();
    }

    @Test
    void parametersInvalidNotList() {
        JavaFileObject javaFile = fromSource(
                "@Command",
                "abstract class Arguments {",
                "",
                "  @Parameters(converter = MyConverter.class)",
                "  abstract Integer something();",
                "",
                "  static class MyConverter extends StringConverter<Integer> {",
                "    public Integer convert(String token) { return null; }",
                "  }",
                "}");
        assertAbout(javaSources()).that(singletonList(javaFile))
                .processedWith(Processor.testInstance())
                .failsToCompile()
                .withErrorContaining("method 'something' is annotated with @Parameters, so it must return java.util.List");
    }

    @Test
    void parametersInvalidNotListEnum() {
        JavaFileObject myEnum = fromSource("enum MyEnum { A }");
        JavaFileObject javaFile = fromSource(
                "@Command",
                "abstract class Arguments {",
                "",
                "  @Parameters",
                "  abstract MyEnum something();",
                "}");
        assertAbout(javaSources()).that(List.of(myEnum, javaFile))
                .processedWith(Processor.testInstance())
                .failsToCompile()
                .withErrorContaining("method 'something' is annotated with @Parameters, so it must return java.util.List");
    }

    @Test
    void parametersInvalidNotListEnumConverter() {
        JavaFileObject myEnum = fromSource("enum MyEnum { A }");
        JavaFileObject javaFile = fromSource(
                "@Command",
                "abstract class Arguments {",
                "",
                "  @Parameters(converter = MyConverter.class)",
                "  abstract MyEnum something();",
                "",
                "  static class MyConverter extends StringConverter<MyEnum> {",
                "    public MyEnum convert(String token) { return null; }",
                "  }",
                "}");
        assertAbout(javaSources()).that(List.of(myEnum, javaFile))
                .processedWith(Processor.testInstance())
                .failsToCompile()
                .withErrorContaining("method 'something' is annotated with @Parameters, so it must return java.util.List");
    }

    @Test
    void parametersInvalidNotListOptional() {
        JavaFileObject javaFile = fromSource(
                "@Command",
                "abstract class Arguments {",
                "",
                "  @Parameters(converter = MyConverter.class)",
                "  abstract Optional<Integer> something();",
                "",
                "  static class MyConverter extends StringConverter<Integer> {",
                "    public Integer convert(String token) { return null; }",
                "  }",
                "}");
        assertAbout(javaSources()).that(singletonList(javaFile))
                .processedWith(Processor.testInstance())
                .failsToCompile()
                .withErrorContaining("method 'something' is annotated with @Parameters, so it must return java.util.List");
    }

    @Test
    void invalidConverterReturnsOptional() {
        JavaFileObject javaFile = fromSource(
                "@Command",
                "abstract class Arguments {",
                "",
                "  @Parameter(converter = MyConverter.class, index = 0)",
                "  abstract Optional<Integer> something();",
                "",
                "  static class MyConverter extends StringConverter<Optional<Integer>> {",
                "    public Integer convert(Optional<Integer> token) { return null; }",
                "  }",
                "}");
        assertAbout(javaSources()).that(singletonList(javaFile))
                .processedWith(Processor.testInstance())
                .failsToCompile()
                .withErrorContaining("invalid converter class: should extend StringConverter<Integer> or implement Supplier<StringConverter<Integer>>");
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
                .withErrorContaining("method 'something' returns a list-based type, so it must be annotated with @Option or @Parameters");
    }

    @Test
    void invalidBounds() {
        JavaFileObject javaFile = fromSource(
                "@Command",
                "abstract class Arguments {",
                "",
                "  @Parameter(index = 0, converter = BoundMapper.class)",
                "  abstract String a();",
                "",
                "  static class BoundMapper<E extends Integer> implements Supplier<StringConverter<E>> {",
                "    public StringConverter<E> get() { return null; }",
                "  }",
                "}");
        assertAbout(javaSources()).that(singletonList(javaFile))
                .processedWith(Processor.testInstance())
                .failsToCompile()
                .withErrorContaining("invalid converter class: type parameters are not allowed in the declaration" +
                        " of converter class 'BoundMapper'");
    }

    @Test
    void indirectSupplier() {
        JavaFileObject javaFile = fromSource(
                "@Command",
                "abstract class Arguments {",
                "",
                "  @Parameter(index = 0, converter = BoundMapper.class)",
                "  abstract String a();",
                "",
                "  static class BoundMapper implements Katz<String> {",
                "    public StringConverter<String> get() { return null; }",
                "  }",
                "",
                "  interface Katz<T> extends Supplier<StringConverter<T>> { }",
                "}");
        assertAbout(javaSources()).that(singletonList(javaFile))
                .processedWith(Processor.testInstance())
                .failsToCompile()
                .withErrorContaining("converter must extend StringConverter<X> or implement Supplier<StringConverter<X>>");
    }

    @Test
    void converterInvalidPrivateConstructor() {
        JavaFileObject javaFile = fromSource(
                "@Command",
                "abstract class Arguments {",
                "",
                "  @Option(names = \"--x\", converter = MapMap.class)",
                "  abstract Integer number();",
                "",
                "  static class MapMap implements Supplier<StringConverter<Integer>> {",
                "",
                "    private MapMap() {}",
                "",
                "    public StringConverter<Integer> get() { return null; }",
                "  }",
                "}");
        assertAbout(javaSources()).that(singletonList(javaFile))
                .processedWith(Processor.testInstance())
                .failsToCompile()
                .withErrorContaining("invalid converter class: invalid constructor: visibility may not be private");
    }

    @Test
    void converterInvalidNoDefaultConstructor() {
        JavaFileObject javaFile = fromSource(
                "@Command",
                "abstract class Arguments {",
                "",
                "  @Option(names = \"--x\", converter = MapMap.class)",
                "  abstract Integer number();",
                "",
                "  static class MapMap implements Supplier<StringConverter<Integer>> {",
                "",
                "    MapMap(int i) {}",
                "",
                "    public StringConverter<Integer> get() { return null; }",
                "  }",
                "}");
        assertAbout(javaSources()).that(singletonList(javaFile))
                .processedWith(Processor.testInstance())
                .failsToCompile()
                .withErrorContaining("invalid converter class: default constructor not found");
    }

    @Test
    void converterInvalidConstructorException() {
        JavaFileObject javaFile = fromSource(
                "@Command",
                "abstract class Arguments {",
                "",
                "  @Option(names = \"--x\", converter = MapMap.class)",
                "  abstract Integer number();",
                "",
                "  static class MapMap implements Supplier<StringConverter<Integer>> {",
                "",
                "    MapMap() throws java.io.IOException {}",
                "",
                "    public StringConverter<Integer> get() { return null; }",
                "  }",
                "}");
        assertAbout(javaSources()).that(singletonList(javaFile))
                .processedWith(Processor.testInstance())
                .failsToCompile()
                .withErrorContaining("invalid converter class: invalid throws clause: found checked exception IOException");
    }

    @Test
    void converterInvalidNonstaticInnerClass() {
        JavaFileObject javaFile = fromSource(
                "@Command",
                "abstract class Arguments {",
                "",
                "  @Option(names = \"--x\", converter = MapMap.class)",
                "  abstract Integer number();",
                "",
                "  class MapMap implements Supplier<StringConverter<Integer>> {",
                "    public StringConverter<Integer> get() { return null; }",
                "  }",
                "}");
        assertAbout(javaSources()).that(singletonList(javaFile))
                .processedWith(Processor.testInstance())
                .failsToCompile()
                .withErrorContaining("invalid converter class: invalid class: nested class 'MapMap' must be static");
    }

    @Test
    void converterInvalidReturnsString() {
        JavaFileObject javaFile = fromSource(
                "@Command",
                "abstract class Arguments {",
                "",
                "  @Option(names = \"--x\", converter = MapMap.class)",
                "  abstract Integer number();",
                "",
                "  static class MapMap implements Supplier<StringConverter<String>> {",
                "    public StringConverter<String> get() { return null; }",
                "  }",
                "}");
        assertAbout(javaSources()).that(singletonList(javaFile))
                .processedWith(Processor.testInstance())
                .failsToCompile()
                .withErrorContaining("invalid converter class: should extend StringConverter<Integer>");
    }

    @Test
    void converterInvalidReturnsStringOptional() {
        JavaFileObject javaFile = fromSource(
                "@Command",
                "abstract class Arguments {",
                "",
                "  @Option(names = \"--x\", converter = MapMap.class)",
                "  abstract java.util.OptionalInt number();",
                "",
                "  static class MapMap implements Supplier<StringConverter<String>> {",
                "    public StringConverter<String> get() { return null; }",
                "  }",
                "}");
        assertAbout(javaSources()).that(singletonList(javaFile))
                .processedWith(Processor.testInstance())
                .failsToCompile()
                .withErrorContaining("invalid converter class: should extend StringConverter<Integer>");
    }

    @Test
    void converterInvalidReturnsStringList() {
        JavaFileObject javaFile = fromSource(
                "@Command",
                "abstract class Arguments {",
                "",
                "  @Option(names = \"--x\", converter = MapMap.class)",
                "  abstract List<Integer> number();",
                "",
                "  static class MapMap implements Supplier<StringConverter<String>> {",
                "    public StringConverter<String> get() { return null; }",
                "  }",
                "}");
        assertAbout(javaSources()).that(singletonList(javaFile))
                .processedWith(Processor.testInstance())
                .failsToCompile()
                .withErrorContaining("invalid converter class: should extend StringConverter<Integer>");
    }

    @Test
    void converterValidTypevars() {
        JavaFileObject javaFile = fromSource(
                "@Command",
                "abstract class Arguments {",
                "",
                "  @Option(names = \"--x\", converter = MapMap.class)",
                "  abstract Supplier<String> string();",
                "",
                "  static class MapMap implements Supplier<StringConverter<Supplier<String>>> {",
                "    public StringConverter<Supplier<String>> get() { return null; }",
                "  }",
                "}");
        assertAbout(javaSources()).that(singletonList(javaFile))
                .processedWith(Processor.testInstance())
                .compilesWithoutError();
    }

    @Test
    void converterValidNestedTypevars() {
        JavaFileObject javaFile = fromSource(
                "@Command",
                "abstract class Arguments {",
                "",
                "  @Option(names = \"--x\", converter = MapMap.class)",
                "  abstract Supplier<Optional<String>> string();",
                "",
                "  static class MapMap implements Supplier<StringConverter<Supplier<Optional<String>>>> {",
                "    public StringConverter<Supplier<Optional<String>>> get() { return null; }",
                "  }",
                "}");

        assertAbout(javaSources()).that(singletonList(javaFile))
                .processedWith(Processor.testInstance())
                .compilesWithoutError();
    }

    @Test
    void converterInvalidRawTypeInSupplier() {
        JavaFileObject javaFile = fromSource(
                "@Command",
                "abstract class Arguments {",
                "",
                "  @Option(names = \"--x\", converter = MapMap.class)",
                "  abstract Integer number();",
                "",
                "  static class MapMap implements Supplier<StringConverter> {",
                "    public StringConverter get() { return null; }",
                "  }",
                "}");
        assertAbout(javaSources()).that(singletonList(javaFile))
                .processedWith(Processor.testInstance())
                .failsToCompile()
                .withErrorContaining("invalid converter class: missing a type parameter in type 'StringConverter'");
    }

    @Test
    void converterInvalidRawSupplier() {
        JavaFileObject javaFile = fromSource(
                "@Command",
                "abstract class Arguments {",
                "",
                "  @Option(names = \"--x\", converter = MapMap.class)",
                "  abstract Integer number();",
                "",
                "  static class MapMap implements Supplier {",
                "    public Object get() { return null; }",
                "  }",
                "}");
        assertAbout(javaSources()).that(singletonList(javaFile))
                .processedWith(Processor.testInstance())
                .failsToCompile()
                .withErrorContaining("invalid converter class: missing a type parameter in type 'Supplier'");
    }

    @Test
    void converterInvalidRawStringConverter() {
        JavaFileObject javaFile = fromSource(
                "@Command",
                "abstract class Arguments {",
                "",
                "  @Option(names = \"--x\", converter = MapMap.class)",
                "  abstract Integer number();",
                "",
                "  static class MapMap extends StringConverter {",
                "    public Object convert(String token) { return null; }",
                "  }",
                "}");
        assertAbout(javaSources()).that(singletonList(javaFile))
                .processedWith(Processor.testInstance())
                .failsToCompile()
                .withErrorContaining("invalid converter class: missing a type parameter in type 'StringConverter'");
    }

    @Test
    void converterValid() {
        JavaFileObject javaFile = fromSource(
                "@Command",
                "abstract class Arguments {",
                "",
                "  @Option(names = \"--x\", converter = MapMap.class)",
                "  abstract List<java.util.OptionalInt> numbers();",
                "",
                "  static class MapMap implements Supplier<StringConverter<java.util.OptionalInt>> {",
                "    public StringConverter<java.util.OptionalInt> get() { return null; }",
                "  }",
                "}");
        assertAbout(javaSources()).that(singletonList(javaFile))
                .processedWith(Processor.testInstance())
                .compilesWithoutError();
    }

    @Test
    void converterValidBytePrimitive() {
        JavaFileObject javaFile = fromSource(
                "@Command",
                "abstract class Arguments {",
                "",
                "  @Option(names = \"--x\", converter = MapMap.class)",
                "  abstract byte number();",
                "",
                "  static class MapMap implements Supplier<StringConverter<Byte>> {",
                "    public StringConverter<Byte> get() { return null; }",
                "  }",
                "}");
        assertAbout(javaSources()).that(singletonList(javaFile))
                .processedWith(Processor.testInstance())
                .compilesWithoutError();
    }

    @Test
    void converterValidOptionalInteger() {
        JavaFileObject javaFile = fromSource(
                "@Command",
                "abstract class Arguments {",
                "",
                "  @Option(names = \"--x\", converter = MapMap.class)",
                "  abstract Optional<Integer> number();",
                "",
                "  static class MapMap extends StringConverter<Integer> {",
                "    public Integer convert(String token) { return null; }",
                "  }",
                "}");
        assertAbout(javaSources()).that(singletonList(javaFile))
                .processedWith(Processor.testInstance())
                .compilesWithoutError();
    }

    @Test
    void implicitMapperOptionalInt() {
        JavaFileObject javaFile = fromSource(
                "@Command",
                "abstract class Arguments {",
                "",
                "  @Option(names = \"--x\", converter = MapMap.class)",
                "  abstract java.util.OptionalInt b();",
                "",
                "  static class MapMap implements Supplier<StringConverter<Integer>> {",
                "    public StringConverter<Integer> get() { return null; }",
                "  }",
                "}");
        assertAbout(javaSources()).that(singletonList(javaFile))
                .processedWith(Processor.testInstance())
                .compilesWithoutError();
    }

    @Test
    void converterOptionalInt() {
        JavaFileObject javaFile = fromSource(
                "@Command",
                "abstract class Arguments {",
                "",
                "  @Option(names = \"--x\", converter = MapMap.class)",
                "  abstract java.util.OptionalInt b();",
                "",
                "  static class MapMap implements Supplier<StringConverter<java.util.OptionalInt>> {",
                "    public StringConverter<java.util.OptionalInt> get() { return null; }",
                "  }",
                "}");
        assertAbout(javaSources()).that(singletonList(javaFile))
                .processedWith(Processor.testInstance())
                .failsToCompile()
                .withErrorContaining("invalid converter class: should extend StringConverter<Integer>");
    }

    @Test
    void converterOptionalInteger() {
        JavaFileObject javaFile = fromSource(
                "@Command",
                "abstract class Arguments {",
                "",
                "  @Option(names = \"--x\", converter = MapMap.class)",
                "  abstract Optional<Integer> b();",
                "",
                "  static class MapMap implements Supplier<StringConverter<Optional<Integer>>> {",
                "    public StringConverter<Optional<Integer>> get() { return null; }",
                "  }",
                "}");
        assertAbout(javaSources()).that(singletonList(javaFile))
                .processedWith(Processor.testInstance())
                .failsToCompile()
                .withErrorContaining("invalid converter class: should extend StringConverter<Integer>");
    }

    @Test
    void oneOptionalInt() {
        JavaFileObject javaFile = fromSource(
                "@Command",
                "abstract class Arguments {",
                "",
                "  @Option(names = \"--x\", converter = MapMap.class)",
                "  abstract java.util.OptionalInt b();",
                "",
                "  static class MapMap implements Supplier<StringConverter<Integer>> {",
                "    public StringConverter<Integer> get() { return null; }",
                "  }",
                "}");
        assertAbout(javaSources()).that(singletonList(javaFile))
                .processedWith(Processor.testInstance())
                .compilesWithoutError();
    }

    @Test
    void converterValidListOfSet() {
        JavaFileObject javaFile = fromSource(
                "@Command",
                "abstract class Arguments {",
                "",
                "  @Option(names = \"--x\", converter = MapMap.class)",
                "  abstract List<java.util.Set<Integer>> sets();",
                "",
                "  static class MapMap implements Supplier<StringConverter<java.util.Set<Integer>>> {",
                "    public StringConverter<java.util.Set<Integer>> get() { return null; }",
                "  }",
                "}");
        assertAbout(javaSources()).that(singletonList(javaFile))
                .processedWith(Processor.testInstance())
                .compilesWithoutError();
    }
}
