package net.jbock.coerce.reference;

import net.jbock.coerce.BasicInfo;
import net.jbock.compiler.EvaluatingProcessor;
import net.jbock.compiler.TypeTool;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

import static net.jbock.coerce.reference.ExpectedType.MAPPER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
      BasicInfo basicInfo = Mockito.mock(BasicInfo.class);
      Mockito.when(basicInfo.tool()).thenReturn(tool);
      Optional<Declared<Supplier>> result = new Resolver(MAPPER, basicInfo).typecheck(mapper, Supplier.class);
      assertTrue(result.isPresent());
      TypeMirror typeMirror = result.get().asType(tool);
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
      BasicInfo basicInfo = Mockito.mock(BasicInfo.class);
      Mockito.when(basicInfo.tool()).thenReturn(tool);
      Optional<Declared<String>> result = new Resolver(MAPPER, basicInfo).typecheck(mapper, String.class);
      assertFalse(result.isPresent());
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
      BasicInfo basicInfo = Mockito.mock(BasicInfo.class);
      Mockito.when(basicInfo.tool()).thenReturn(tool);
      Optional<Declared<Function>> result = new Resolver(MAPPER, basicInfo).typecheck(functionType, Function.class);
      assertTrue(result.isPresent());
    });
  }
}
