package net.jbock.coerce;

import com.squareup.javapoet.CodeBlock;
import net.jbock.coerce.collectorpresent.CollectorClassValidator;
import net.jbock.coerce.collectors.CollectorInfo;
import net.jbock.compiler.EvaluatingProcessor;
import net.jbock.compiler.TypeTool;
import org.junit.jupiter.api.Test;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import java.util.Collections;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
      DeclaredType originalReturnType = tool.getDeclaredType(Set.class, Collections.singletonList(tool.asType(String.class)));
      TypeElement collectorClass = elements.getTypeElement("ToSetCollector");
      CollectorInfo collectorInfo = new CollectorClassValidator(s -> null, tool, collectorClass, originalReturnType)
          .getCollectorInfo();
      CodeBlock expected = CodeBlock.of(".collect(new $T<$T>().get())", types.erasure(collectorClass.asType()), String.class);
      assertEquals(expected, collectorInfo.collectExpr());
    });
  }
}
