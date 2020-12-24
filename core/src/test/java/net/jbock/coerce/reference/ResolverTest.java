package net.jbock.coerce.reference;

import net.jbock.coerce.either.Either;
import net.jbock.coerce.either.Left;
import net.jbock.compiler.EvaluatingProcessor;
import net.jbock.compiler.TypeTool;
import net.jbock.compiler.ValidationException;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import java.util.List;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ResolverTest {

  @Test
  void testTypecheckSuccess() {

    EvaluatingProcessor.source(
        "package test;",
        "",
        "import java.util.function.Supplier;",
        "",
        "interface StringSupplier extends Supplier<String> { }",
        "",
        "abstract class Foo implements StringSupplier { }"
    ).run("Mapper", (elements, types) -> {
      TypeTool tool = new TypeTool(elements, types);
      TypeElement mapper = elements.getTypeElement("test.Foo");
      Resolver resolver = new Resolver(tool, message -> ValidationException.create(Mockito.mock(Element.class), ""));
      Either<TypecheckFailure, List<? extends TypeMirror>> result = resolver.checkImplements(mapper, Supplier.class);
      assertTrue(result instanceof Left);
    });
  }

  @Test
  void testTypecheckFail() {

    EvaluatingProcessor.source(
        "package test;",
        "",
        "import java.util.function.Supplier;",
        "",
        "interface StringSupplier extends Supplier<String> { }",
        "",
        "abstract class Foo implements StringSupplier { }"
    ).run("Mapper", (elements, types) -> {
      TypeTool tool = new TypeTool(elements, types);
      TypeElement mapper = elements.getTypeElement("test.Foo");
      Resolver resolver = new Resolver(tool, message -> ValidationException.create(Mockito.mock(Element.class), ""));
      Either<TypecheckFailure, List<? extends TypeMirror>> result = resolver.checkImplements(mapper, String.class);
      assertTrue(result instanceof Left);
    });
  }
}
