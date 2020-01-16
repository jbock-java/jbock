package net.jbock.coerce.reference;

import net.jbock.coerce.either.Either;
import net.jbock.coerce.either.Left;
import net.jbock.coerce.either.Right;
import net.jbock.compiler.EvaluatingProcessor;
import net.jbock.compiler.TypeTool;
import org.junit.jupiter.api.Test;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import java.util.function.Function;
import java.util.function.Supplier;

import static net.jbock.coerce.reference.ExpectedType.MAPPER;
import static org.junit.jupiter.api.Assertions.assertEquals;
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
      Either<TypecheckFailure, Declared<Supplier>> result = new Resolver(MAPPER, tool).typecheck(mapper, Supplier.class);
      assertTrue(result instanceof Right);
      TypeMirror typeMirror = ((Right<TypecheckFailure, Declared<Supplier>>) result).value().asType(tool);
      DeclaredType declared = TypeTool.asDeclared(typeMirror);
      assertEquals(1, declared.getTypeArguments().size());
      TypeMirror typeParameter = declared.getTypeArguments().get(0);
      TypeElement string = elements.getTypeElement("java.lang.String");
      assertTrue(types.isSameType(string.asType(), typeParameter));
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
      Either<TypecheckFailure, Declared<String>> result = new Resolver(MAPPER, tool).typecheck(mapper, String.class);
      assertTrue(result instanceof Left);
    });
  }

  @Test
  void testTypecheckFunction() {

    EvaluatingProcessor.source(
        "package test;",
        "",
        "import java.util.function.Supplier;",
        "import java.util.function.Function;",
        "",
        "interface FunctionSupplier extends Supplier<Function<String, String>> { }"
    ).run("Mapper", (elements, types) -> {
      TypeTool tool = new TypeTool(elements, types);
      TypeElement mapper = elements.getTypeElement("test.FunctionSupplier");
      DeclaredType declaredType = TypeTool.asDeclared(mapper.getInterfaces().get(0));
      DeclaredType functionType = TypeTool.asDeclared(declaredType.getTypeArguments().get(0));
      Either<TypecheckFailure, Declared<Function>> result = new Resolver(MAPPER, tool).typecheck(functionType, Function.class);
      assertTrue(result instanceof Right);
    });
  }
}
