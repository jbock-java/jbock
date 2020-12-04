package net.jbock.coerce.reference;

import net.jbock.compiler.EvaluatingProcessor;
import net.jbock.compiler.TypeTool;
import org.junit.jupiter.api.Test;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings("rawtypes")
class ReferenceToolTest {

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
      TypeMirror AA1 = mapperClass.getTypeParameters().get(0).asType();
      TypeMirror AA2 = mapperClass.getTypeParameters().get(1).asType();
      TypeTool tool = new TypeTool(elements, types);
      ReferenceTool<Function> referenceTool = new ReferenceTool<>(ExpectedType.FUNCTION, s -> null, tool, mapperClass);
      ReferencedType<Function> referencedType = referenceTool.getReferencedType();
      assertTrue(referencedType.isSupplier());
      assertEquals(2, referencedType.typeArguments().size());
      assertTrue(types.isSameType(AA1, referencedType.typeArguments().get(0)));
      assertTrue(types.isSameType(tool.getDeclaredType(List.class, Collections.singletonList(AA2)), referencedType.typeArguments().get(1)));
    });
  }
}