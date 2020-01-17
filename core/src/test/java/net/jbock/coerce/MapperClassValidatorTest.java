package net.jbock.coerce;

import net.jbock.coerce.either.Either;
import net.jbock.coerce.either.Right;
import net.jbock.coerce.mapper.ReferenceMapperType;
import net.jbock.compiler.EvaluatingProcessor;
import net.jbock.compiler.TypeExpr;
import net.jbock.compiler.TypeTool;
import org.junit.jupiter.api.Test;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MapperClassValidatorTest {

  @Test
  void simpleTest() {

    EvaluatingProcessor.source(
        "import java.util.List;",
        "import java.util.function.Supplier;",
        "import java.util.function.Function;",
        "",
        "class Mapper<AA1, AA2> implements A<AA1, AA2> { public Function<AA1, List<AA2>> get() { return null; } }",
        "interface A<BB1, BB2> extends B<BB1, List<BB2>> { }",
        "interface B<CC1, CC2> extends C<CC1, CC2> { }",
        "interface C<DD1, DD2> extends Supplier<Function<DD1, DD2>> { }"
    ).run("Mapper", (elements, types) -> {
      TypeElement mapperClass = elements.getTypeElement("Mapper");
      TypeTool tool = new TypeTool(elements, types);
      DeclaredType expectedReturnType = TypeExpr.prepare(elements, types).parse("java.util.List<java.lang.Integer>");
      Either<String, ReferenceMapperType> mapperType = new MapperClassValidator(s -> null, tool, expectedReturnType, mapperClass)
          .checkReturnType();
      assertTrue(mapperType instanceof Right);
      ReferenceMapperType value = ((Right<String, ReferenceMapperType>) mapperType).value();
      assertEquals(2, value.solution().size());
      assertTrue(tool.isSameType(value.solution().get(0), String.class));
      assertTrue(tool.isSameType(value.solution().get(1), Integer.class));
    });
  }
}
