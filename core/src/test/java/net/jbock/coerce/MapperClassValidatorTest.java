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
        "interface Mapper<AA1, AA2> extends A<AA1, AA2> { }",
        "interface A<BB1, BB2> extends B<BB1, List<BB2>> { }",
        "interface B<CC1, CC2> extends C<CC1, CC2> { }",
        "interface C<DD1, DD2> extends Supplier<Function<DD1, DD2>> { }"
    ).run("Mapper", (elements, types) -> {
      TypeElement mapperClass = elements.getTypeElement("Mapper");

      BasicInfo basicInfo = mock(BasicInfo.class);
      TypeTool tool = new TypeTool(elements, types);
      when(basicInfo.tool()).thenReturn(tool);

      DeclaredType expectedReturnType = TypeExpr.prepare(elements, types).parse("java.util.List<java.lang.Integer>");

      MapperType mapperType = new MapperClassValidator(basicInfo, expectedReturnType, mapperClass)
          .checkReturnType()
          .orElseThrow(AssertionError::new);
      assertEquals(2, mapperType.solution().size());
      assertTrue(tool.isSameType(mapperType.solution().get(0), String.class));
      assertTrue(tool.isSameType(mapperType.solution().get(1), Integer.class));
    });
  }
}
