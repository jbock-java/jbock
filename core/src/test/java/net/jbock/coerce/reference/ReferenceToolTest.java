package net.jbock.coerce.reference;

import net.jbock.compiler.EvaluatingProcessor;
import net.jbock.compiler.TypeTool;
import net.jbock.either.Either;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.lang.model.element.TypeElement;

class ReferenceToolTest {

  @Test
  void testTypecheckSuccess() {

    EvaluatingProcessor.source(
        "package test;",
        "",
        "import java.util.function.Supplier;",
        "import java.util.function.Function;",
        "import java.util.Set;",
        "", "",
        "abstract class Foo implements Supplier<Function<String, Set<String>>> { }"
    ).run("Mapper", (elements, types) -> {
      TypeTool tool = new TypeTool(elements, types);
      TypeElement typeElement = elements.getTypeElement("test.Foo");
      ReferenceTool referenceTool = new ReferenceTool(tool, typeElement);
      Either<String, FunctionType> result = referenceTool.getReferencedType();
      result.accept(l -> Assertions.fail(), functionType -> Assertions.assertTrue(functionType.isSupplier()));
    });
  }
}
