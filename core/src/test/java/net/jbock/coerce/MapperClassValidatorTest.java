package net.jbock.coerce;

import net.jbock.coerce.mapper.MapperType;
import net.jbock.compiler.EvaluatingProcessor;
import net.jbock.compiler.TypeExpr;
import net.jbock.compiler.TypeTool;
import org.junit.jupiter.api.Test;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MapperClassValidatorTest {

  @Test
  void simpleTest() {

    EvaluatingProcessor.source(
        "import java.util.List;",
        "import java.util.function.Supplier;",
        "import java.util.function.Function;",
        "",
        "interface Mapper<F, T> extends A<F, T> { }",
        "interface A<R, X> extends B<X, List<R>> { }",
        "interface B<F, K> extends C<F, K> { }",
        "interface C<X, R> extends Supplier<Function<X, R>> { }"
    ).run("Mapper", (elements, types) -> {
      TypeElement mapperClass = elements.getTypeElement("Mapper");

      BasicInfo basicInfo = mock(BasicInfo.class);
      TypeTool tool = new TypeTool(elements, types);
      when(basicInfo.tool()).thenReturn(tool);

      DeclaredType expectedReturnType = TypeExpr.prepare(elements, types).parse("java.util.List<java.lang.Integer>");

      MapperType mapperType = new MapperClassValidator(basicInfo, expectedReturnType, mapperClass)
          .checkReturnType();
      assertEquals(2, mapperType.solution().size());
      assertTrue(tool.isSameType(mapperType.solution().get(0), Integer.class));
      assertTrue(tool.isSameType(mapperType.solution().get(1), String.class));
    });
  }
}
