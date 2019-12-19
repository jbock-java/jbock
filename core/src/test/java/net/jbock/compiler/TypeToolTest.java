package net.jbock.compiler;

import org.junit.jupiter.api.Test;

import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

import static net.jbock.compiler.EvaluatingProcessor.assertSameType;
import static net.jbock.compiler.TypeTool.asDeclared;
import static org.junit.jupiter.api.Assertions.assertEquals;

class TypeToolTest {

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
