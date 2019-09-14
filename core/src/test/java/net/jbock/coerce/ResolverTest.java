package net.jbock.coerce;

import net.jbock.coerce.Resolver.ImplementsRelation;
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
      Optional<TypeMirror> result = Resolver.typecheck(mapper, Supplier.class, tool);
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
      Optional<TypeMirror> result = Resolver.typecheck(mapper, String.class, tool);
      assertFalse(result.isPresent());
    });
  }

  @Test
  void testToAnimal() {

    EvaluatingProcessor.source(
        "package test;",
        "import java.util.function.Function;",
        "import java.util.List;",
        "",
        "import java.util.function.Supplier;",
        "",
        "interface Mapper<A> extends F<A, String> { }",
        "interface F<V, T> extends Function<T, List<V>> { }"
    ).run("Mapper", (elements, types) -> {
      TypeTool tool = new TypeTool(elements, types);
      TypeElement mapper = elements.getTypeElement("test.Mapper");
      assertEquals(1, mapper.getInterfaces().size());
      TypeMirror x = mapper.getInterfaces().get(0);
      TypeElement f = elements.getTypeElement("test.F");
      Resolver resolver = new Resolver(tool);
      ImplementsRelation relation = new ImplementsRelation(f, f.getInterfaces().get(0));
      TypeMirror result = resolver.toAnimal(x, relation);
      assertEquals("java.util.function.Function<java.lang.String,java.util.List<A>>", result.toString());
    });
  }
}
