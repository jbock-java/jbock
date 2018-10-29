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

import static net.jbock.compiler.TypeTool.asDeclared;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TypeToolTest {

  @Test
  void simpleTest() {

    EvaluatingProcessor.source(
        "import java.util.Set;",
        "",
        "abstract class Foo { abstract <E> Set<E> getSet(); }"
    ).run(context -> {
      TypeElement set = context.elements().getTypeElement("java.util.Set");
      TypeElement string = context.elements().getTypeElement("java.lang.String");
      List<ExecutableElement> methods = ElementFilter.methodsIn(context.elements().getTypeElement("Foo").getEnclosedElements());
      ExecutableElement getSetMethod = methods.get(0);
      TypeMirror returnType = getSetMethod.getReturnType();
      TypeTool tool = TypeTool.get();
      Optional<Map<String, TypeMirror>> result = tool.unify(context.types().getDeclaredType(set, string.asType()), returnType);
      assertTrue(result.isPresent());
      Map<String, TypeMirror> solution = result.get();
      assertTrue(solution.containsKey("E"));
      TypeMirror value = solution.get("E");
      assertTrue(context.types().isSameType(value, string.asType()));
    });
  }

  @Test
  void substituteTest() {

    EvaluatingProcessor.source().run(context -> {
      TypeElement string = context.elements().getTypeElement("java.lang.String");
      Optional<TypeMirror> substitute = TypeTool.get().substitute(string.asType(), Collections.emptyMap());
      assertTrue(substitute.isPresent());
    });
  }

  @Test
  void testToType() {
    EvaluatingProcessor.source().run(context -> {
      DeclaredType map = context.declared("java.util.Map<java.util.List<java.lang.String>, java.lang.String>");
      assertEquals(context.types().erasure(context.elements().getTypeElement("java.util.Map").asType()), context.types().erasure(map));
      assertEquals(2, map.getTypeArguments().size());
      TypeMirror key = map.getTypeArguments().get(0);
      TypeMirror value = map.getTypeArguments().get(1);
      context.assertSameType(context.types().erasure(context.elements().getTypeElement("java.util.List").asType()), context.types().erasure(key));
      assertEquals(1, asDeclared(key).getTypeArguments().size());
      context.assertSameType(context.types().getDeclaredType(context.elements().getTypeElement("java.lang.String")), asDeclared(key).getTypeArguments().get(0));
      context.assertSameType(context.types().getDeclaredType(context.elements().getTypeElement("java.lang.String")), value);
    });
  }
}