package net.jbock.coerce;

import net.jbock.coerce.collector.CustomCollector;
import net.jbock.compiler.EvaluatingProcessor;
import net.jbock.compiler.TypeExpr;
import net.jbock.compiler.TypeTool;
import org.junit.jupiter.api.Test;

import javax.lang.model.element.TypeElement;

import static net.jbock.compiler.EvaluatingProcessor.assertSameType;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
      when(basicInfo.originalReturnType()).thenReturn(TypeExpr.prepare(elements, types).parse(
          "java.util.Set<java.lang.String>"));

      TypeElement collectorClass = elements.getTypeElement("ToSetCollector");
      CustomCollector collectorInfo = new CollectorClassValidator(basicInfo, collectorClass)
          .getCollectorInfo();
      assertSameType("java.lang.String", collectorInfo.inputType(), elements, types);
      assertNotNull(collectorInfo.collectorType());
    });
  }
}
