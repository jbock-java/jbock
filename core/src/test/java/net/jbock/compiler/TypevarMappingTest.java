package net.jbock.compiler;

import net.jbock.coerce.either.Either;
import net.jbock.coerce.either.Right;
import net.jbock.coerce.reference.TypecheckFailure;
import org.junit.jupiter.api.Test;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import java.util.Collections;
import java.util.List;

import static net.jbock.compiler.TypeTool.AS_DECLARED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TypevarMappingTest {

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
      Either<String, TypevarMapping> result = tool.unify(types.getDeclaredType(set, string.asType()), returnType);
      assertTrue(result instanceof Right);
      TypevarMapping solution = ((Right<String, TypevarMapping>) result).value();
      assertNotNull(solution.get("E"));
      TypeMirror value = solution.get("E");
      assertTrue(types.isSameType(value, string.asType()));
    });
  }

  @Test
  void substituteTest() {

    EvaluatingProcessor.source().run((elements, types) -> {
      TypeTool tool = new TypeTool(elements, types);
      TypeElement string = elements.getTypeElement("java.lang.String");
      TypevarMapping mapping = new TypevarMapping(Collections.emptyMap(), tool);
      Either<TypecheckFailure, TypeMirror> substitute = mapping.substitute(string.asType());
      assertNotNull(substitute);
      assertTrue(substitute instanceof Right);
    });
  }

  @Test
  void substituteTestSet() {

    EvaluatingProcessor.source(
        "package a;",
        "",
        "interface Set<E> {}"
    ).run((elements, types) -> {
      TypeTool tool = new TypeTool(elements, types);
      TypeMirror setOfE = elements.getTypeElement("a.Set").asType();
      TypeElement boxInt = elements.getTypeElement("java.lang.Integer");
      TypevarMapping mapping = new TypevarMapping(Collections.singletonMap("E", boxInt.asType()), tool);
      Either<TypecheckFailure, DeclaredType> either = mapping.substitute(
          setOfE.accept(AS_DECLARED, null));
      assertTrue(either instanceof Right);
      DeclaredType result = ((Right<TypecheckFailure, DeclaredType>) either)
          .value().accept(AS_DECLARED, null);
      assertNotNull(result);
      assertTrue(types.isSameType(types.erasure(result), types.erasure(elements.getTypeElement("a.Set").asType())));
      assertEquals(1, result.getTypeArguments().size());
      assertTrue(types.isSameType(boxInt.asType(), result.getTypeArguments().get(0)));
    });
  }
}