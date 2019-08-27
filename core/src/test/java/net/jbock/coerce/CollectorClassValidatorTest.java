package net.jbock.coerce;

import net.jbock.compiler.EvaluatingProcessor;
import net.jbock.compiler.TypeExpr;
import net.jbock.compiler.TypeTool;
import org.junit.jupiter.api.Test;

import javax.lang.model.element.TypeElement;

import static net.jbock.compiler.EvaluatingProcessor.assertSameType;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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

      BasicInfo basicInfo = mock(BasicInfo.class);
      when(basicInfo.tool()).thenReturn(new TypeTool(elements, types));
      when(basicInfo.returnType()).thenReturn(TypeExpr.prepare(elements, types).parse(
          "java.util.Set<java.lang.String>"));

      TypeElement collectorClass = elements.getTypeElement("ToSetCollector");
      CollectorInfo collectorInfo = CollectorClassValidator.getCollectorInfo(
          collectorClass,
          basicInfo);
      assertSameType("java.lang.String", collectorInfo.inputType, elements, types);
      assertTrue(collectorInfo.collectorType().isPresent());
      assertSameType("ToSetCollector<java.lang.String>",
          collectorInfo.collectorType().get(), elements, types);
    });
  }
}
