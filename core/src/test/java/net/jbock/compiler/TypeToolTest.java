package net.jbock.compiler;

import org.junit.jupiter.api.Test;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TypeToolTest {

  @Test
  void simpleTest() {

    EvaluatingProcessor.source(
        "import java.util.Set;",
        "",
        "abstract class Foo { abstract <E> Set<E> getSet(); }"
    ).run((elements, types) -> {
      TypeElement foo = elements.getTypeElement("Foo");
      List<ExecutableElement> methods = ElementFilter.methodsIn(foo.getEnclosedElements());
      ExecutableElement getSet = methods.get(0);
      TypeMirror returnType = getSet.getReturnType();
      assertTrue(TypeTool.get().isAssignable(
          TypeTool.get().declared("java.util.Set", "java.lang.String"), returnType));
      assertFalse(TypeTool.get().isAssignable(
          TypeTool.get().declared("java.util.List", "java.lang.String"), returnType));
    });
  }

  @Test
  void mapTest() {

    EvaluatingProcessor.source(
        "import java.util.Map;",
        "",
        "abstract class Foo { abstract <E> Map<E, Map<String, E>> getMap(); }"
    ).run((elements, types) -> {
      TypeElement foo = elements.getTypeElement("Foo");
      List<ExecutableElement> methods = ElementFilter.methodsIn(foo.getEnclosedElements());
      ExecutableElement getSet = methods.get(0);
      TypeMirror returnType = getSet.getReturnType();
      TypeTool tt = TypeTool.get();
      assertTrue(tt.isAssignable(
          tt.declared("java.util.Map", "java.lang.String", tt.declared("java.util.Map", "java.lang.String", "java.lang.String")), returnType));
      assertTrue(tt.isAssignable(
          tt.declared("java.util.Map", "java.lang.Integer", tt.declared("java.util.Map", "java.lang.String", "java.lang.Integer")), returnType));
      assertFalse(tt.isAssignable(
          tt.declared("java.util.Map", "java.lang.Integer", tt.declared("java.util.Map", "java.lang.Integer", "java.lang.String")), returnType));
    });
  }
}