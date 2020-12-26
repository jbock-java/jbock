package net.jbock.coerce.reference;

import net.jbock.compiler.EvaluatingProcessor;
import net.jbock.compiler.TypeTool;
import net.jbock.compiler.ValidationException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.util.function.Function;
import java.util.stream.Collector;

import static org.mockito.Mockito.mock;

class ReferenceToolTest {

  private final Function<String, ValidationException> errorHandler = message -> ValidationException.create(mock(Element.class), "");

  @Test
  void testTypecheckSuccess() {

    EvaluatingProcessor.source(
        "package test;",
        "",
        "import java.util.function.Supplier;",
        "import java.util.stream.Collector;",
        "import java.util.Set;",
        "", "",
        "abstract class Foo<E> implements Supplier<Collector<E, ?, Set<E>>> { }"
    ).run("Mapper", (elements, types) -> {
      TypeTool tool = new TypeTool(elements, types);
      TypeElement typeElement = elements.getTypeElement("test.Foo");
      ReferenceTool<Collector<?, ?, ?>> referenceTool = new ReferenceTool<>(ExpectedType.COLLECTOR, errorHandler, tool, typeElement);
      ReferencedType<Collector<?, ?, ?>> result = referenceTool.getReferencedType();
      Assertions.assertTrue(result.isSupplier());
      Assertions.assertEquals(3, result.typeArguments().size());
    });
  }
}
