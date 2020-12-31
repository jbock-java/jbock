package net.jbock.coerce;

import com.squareup.javapoet.CodeBlock;
import net.jbock.compiler.EvaluatingProcessor;
import net.jbock.compiler.TypeTool;
import net.jbock.compiler.ValidationException;
import org.junit.jupiter.api.Test;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class CollectorClassValidatorTest {

  @Test
  void simpleTest() {

    EvaluatingProcessor.source(
        "import java.util.Set;",
        "import java.util.function.Supplier;",
        "import java.util.stream.Collector;",
        "import java.util.stream.Collectors;",
        "",
        "class ToSetCollector<E> implements Supplier<Collector<E, ?, Set<E>>> {",
        "  public Collector<E, ?, Set<E>> get() {",
        "    return Collectors.toSet();",
        "  }",
        "}"
    ).run("ToSetCollector", (elements, types) -> {

      TypeTool tool = new TypeTool(elements, types);
      DeclaredType returnType = getDeclaredType(tool, Set.class, Collections.singletonList(tool.asTypeElement(String.class.getCanonicalName()).asType()));
      TypeElement collectorClass = elements.getTypeElement("ToSetCollector");
      CollectorInfo collectorInfo = new CollectorClassValidator(s -> ValidationException.create(mock(Element.class), s),
          tool, collectorClass, returnType)
          .getCollectorInfo();
      CodeBlock expected = CodeBlock.of(".collect(new $T<$T>().get())", types.erasure(collectorClass.asType()), String.class);
      assertEquals(expected, collectorInfo.collectExpr());
    });
  }

  private DeclaredType getDeclaredType(
      TypeTool tool,
      Class<?> clazz,
      List<? extends TypeMirror> typeArguments) {
    TypeElement element = tool.asTypeElement(clazz.getCanonicalName());
    Types types = tool.types();
    return types.getDeclaredType(element, typeArguments.toArray(new TypeMirror[0]));
  }
}
