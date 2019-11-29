package net.jbock.compiler;

import org.junit.jupiter.api.Test;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static net.jbock.compiler.EvaluatingProcessor.assertSameType;
import static net.jbock.compiler.TypeTool.AS_DECLARED;
import static net.jbock.compiler.TypeTool.asDeclared;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TypeToolTest {

  @Test
  void simpleTest() {

    EvaluatingProcessor.source(
        "import java.util.Set;",
        "",
        "abstract class Foo { abstract <E> Set<E> getSet(); }"
    ).run((elements, types) -> {
      TypeTool tool = new TypeTool(elements, types);
      TypeElement set = elements.getTypeElement("java.util.Set");
      TypeElement string = elements.getTypeElement("java.lang.String");
      List<ExecutableElement> methods = ElementFilter.methodsIn(elements.getTypeElement("Foo").getEnclosedElements());
      ExecutableElement getSetMethod = methods.get(0);
      TypeMirror returnType = getSetMethod.getReturnType();
      Optional<Map<String, TypeMirror>> result = tool.unify(types.getDeclaredType(set, string.asType()), returnType);
      assertTrue(result.isPresent());
      Map<String, TypeMirror> solution = result.get();
      assertTrue(solution.containsKey("E"));
      TypeMirror value = solution.get("E");
      assertTrue(types.isSameType(value, string.asType()));
    });
  }

  @Test
  void substituteTest() {

    EvaluatingProcessor.source().run((elements, types) -> {
      TypeTool tool = new TypeTool(elements, types);
      TypeElement string = elements.getTypeElement("java.lang.String");
      TypeMirror substitute = tool.substitute(string.asType(), Collections.emptyMap())
          .orElseThrow(AssertionError::new);
      assertNotNull(substitute);
    });
  }

  @Test
  void substituteTestWithInvalidBounds() {

    EvaluatingProcessor.source(
        "package a;",
        "",
        "interface Set<E extends Number> {}"
    ).run((elements, types) -> {
      TypeTool tool = new TypeTool(elements, types);
      TypeMirror setOfE = elements.getTypeElement("a.Set").asType();
      TypeElement string = elements.getTypeElement("java.lang.String");
      Optional<DeclaredType> result = tool.substitute(
          setOfE.accept(AS_DECLARED, null),
          Collections.singletonMap("E", string.asType()));
      assertFalse(result.isPresent());
    });
  }

  @Test
  void substituteTestWithValidBounds() {

    EvaluatingProcessor.source(
        "package a;",
        "",
        "interface Set<E extends Number> {}"
    ).run((elements, types) -> {
      TypeTool tool = new TypeTool(elements, types);
      TypeMirror setOfE = elements.getTypeElement("a.Set").asType();
      TypeElement boxInt = elements.getTypeElement("java.lang.Integer");
      DeclaredType result = tool.substitute(
          setOfE.accept(AS_DECLARED, null),
          Collections.singletonMap("E", boxInt.asType()))
          .orElseThrow(AssertionError::new)
          .accept(AS_DECLARED, null);
      assertNotNull(result);
      assertTrue(types.isSameType(types.erasure(result), types.erasure(elements.getTypeElement("a.Set").asType())));
      assertEquals(1, result.getTypeArguments().size());
      assertTrue(types.isSameType(boxInt.asType(), result.getTypeArguments().get(0)));
    });
  }

  @Test
  void testToType() {
    EvaluatingProcessor.source().run((elements, types) -> {
      DeclaredType map = TypeExpr.prepare(elements, types).parse(
          "java.util.Map<java.util.List<java.lang.String>, java.lang.String>");
      assertEquals(types.erasure(elements.getTypeElement("java.util.Map").asType()), types.erasure(map));
      assertEquals(2, map.getTypeArguments().size());
      TypeMirror key = map.getTypeArguments().get(0);
      TypeMirror value = map.getTypeArguments().get(1);
      assertSameType(
          types.erasure(elements.getTypeElement("java.util.List").asType()),
          types.erasure(key),
          types);
      assertEquals(1, asDeclared(key).getTypeArguments().size());
      assertSameType(
          types.getDeclaredType(elements.getTypeElement("java.lang.String")),
          asDeclared(key).getTypeArguments().get(0),
          types);
      assertSameType(
          types.getDeclaredType(elements.getTypeElement("java.lang.String")),
          value,
          types);
    });
  }
}
