package net.jbock.coerce;

import net.jbock.compiler.EvaluatingProcessor;
import org.junit.jupiter.api.Test;

import javax.lang.model.element.TypeElement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MapperClassValidatorTest {

  @Test
  void simpleTest() {

    EvaluatingProcessor.source(
        "import java.util.function.Supplier;",
        "import java.util.function.Function;",
        "",
        "class Mapper implements A<Integer, String> {",
        "  public Function<String, Integer> get() {",
        "    return null;",
        "  }",
        "}",
        "",
        "interface A<R, X> extends B<X, R> { }",
        "interface B<X, R> extends C<X, R> { }",
        "interface C<X, R> extends Supplier<Function<X, R>> { }"
    ).run("Mapper", context -> {
      TypeElement mapperClass = context.elements().getTypeElement("Mapper");

      // no exception: mapper returns Integer indeed
      MapperClassValidator.checkReturnType(mapperClass, context.declared("java.lang.Integer"));

      // exception: mapper doesn't return String
      assertEquals("There is a problem with the mapper class: The mapper should return java.lang.String but returns java.lang.Integer.",
          assertThrows(TmpException.class, () -> MapperClassValidator.checkReturnType(mapperClass, context.declared("java.lang.String"))).getMessage());
    });
  }
}