package net.jbock.coerce;

import net.jbock.compiler.EvaluatingProcessor;
import net.jbock.compiler.TypeTool;
import org.junit.jupiter.api.Test;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import java.util.Optional;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ResolverTest {

  @Test
  void resolves() {

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
      Optional<TypeMirror> result = Resolver.typecheck(Supplier.class, mapper.asType(), tool);
      assertTrue(result.isPresent());
      TypeMirror typeMirror = result.get();
      DeclaredType declared = TypeTool.asDeclared(typeMirror);
      assertEquals(1, declared.getTypeArguments().size());
      TypeMirror typeParameter = declared.getTypeArguments().get(0);
      TypeElement string = elements.getTypeElement("java.lang.String");
      assertTrue(types.isSameType(string.asType(), typeParameter));
    });
  }

  @Test
  void doesNotResolve() {

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
      Optional<TypeMirror> result = Resolver.typecheck(String.class, mapper.asType(), tool);
      assertFalse(result.isPresent());
    });
  }
}