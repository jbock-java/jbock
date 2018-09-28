package net.jbock.coerce;

import org.junit.jupiter.api.Test;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import java.util.List;

class CollectorClassValidatorTest {

  @Test
  void runTests() {

    EvaluatingProcessor.source(
        "import java.util.Set;",
        "",
        "abstract class Foo { abstract <E> Set<E> getSet(); }"
    ).run((elements, types) -> {
      TypeElement foo = elements.getTypeElement("Foo");
      List<ExecutableElement> methods = ElementFilter.methodsIn(foo.getEnclosedElements());
      ExecutableElement getSet = methods.get(0);
      TypeMirror getSetReturnType = getSet.getReturnType();
      DeclaredType setOfString = types.getDeclaredType(elements.getTypeElement("java.util.Set"),
          elements.getTypeElement("java.lang.String").asType());
      System.out.println(types.isAssignable(getSetReturnType, setOfString));
      System.out.println(types.isAssignable(setOfString, getSetReturnType));
    });
  }
}