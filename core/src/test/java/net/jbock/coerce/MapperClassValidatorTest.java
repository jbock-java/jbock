package net.jbock.coerce;

import net.jbock.compiler.EvaluatingProcessor;
import net.jbock.compiler.TypeExpr;
import net.jbock.compiler.TypeTool;
import net.jbock.compiler.ValidationException;
import org.junit.jupiter.api.Test;

import javax.lang.model.element.TypeElement;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
    ).run("Mapper", (elements, types) -> {
      TypeElement mapperClass = elements.getTypeElement("Mapper");

      BasicInfo basicInfo = mock(BasicInfo.class);
      when(basicInfo.tool()).thenReturn(new TypeTool(elements, types));
      when(basicInfo.asValidationException(anyString())).thenReturn(ValidationException.create(null, ""));

      // no exception: mapper returns Integer indeed
      new MapperClassValidator(basicInfo, TypeExpr.prepare(elements, types)
          .parse("java.lang.Integer"))
          .checkReturnType(mapperClass);

      // exception: mapper doesn't return String
      assertThrows(ValidationException.class, () -> new MapperClassValidator(basicInfo, TypeExpr.prepare(elements, types)
          .parse("java.lang.String"))
          .checkReturnType(mapperClass)).getMessage();
    });
  }
}
