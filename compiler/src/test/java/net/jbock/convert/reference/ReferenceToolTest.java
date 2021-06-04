package net.jbock.convert.reference;

import net.jbock.common.SafeElements;
import net.jbock.common.TypeTool;
import net.jbock.either.Either;
import net.jbock.processor.EvaluatingProcessor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.lang.model.element.TypeElement;

class ReferenceToolTest {

  @Test
  void testTypecheckSuccess() {

    EvaluatingProcessor.source(
        "package test;",
        "",
        "import net.jbock.util.StringConverter;",
        "import java.util.function.Supplier;",
        "import java.util.function.Function;",
        "import java.util.Set;",
        "", "",
        "abstract class Foo implements Supplier<StringConverter<Set<String>>> { }"
    ).run("Mapper", (elements, types) -> {
      TypeTool tool = new TypeTool(new SafeElements(elements), types);
      TypeElement typeElement = elements.getTypeElement("test.Foo");
      ReferenceTool referenceTool = new ReferenceTool(tool);
      Either<String, StringConverterType> result = referenceTool.getReferencedType(typeElement);
      result.accept(l -> Assertions.fail(), functionType -> Assertions.assertTrue(functionType.isSupplier()));
    });
  }
}
