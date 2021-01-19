package net.jbock.coerce.reference;

import net.jbock.either.Either;
import net.jbock.compiler.EvaluatingProcessor;
import net.jbock.compiler.TypeTool;
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
        "abstract class Foo<E> implements Supplier<Function<E, Set<E>>> { }"
    ).run("Mapper", (elements, types) -> {
      TypeTool tool = new TypeTool(elements, types);
      TypeElement typeElement = elements.getTypeElement("test.Foo");
      ReferenceTool referenceTool = new ReferenceTool(tool, typeElement);
      Either<String, FunctionType> result = referenceTool.getReferencedType();
      Assertions.assertTrue(result.isPresent());
      result.ifPresent(functionType -> Assertions.assertTrue(functionType.isSupplier()));
    });
  }
}
