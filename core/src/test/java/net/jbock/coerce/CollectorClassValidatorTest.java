package net.jbock.coerce;

import net.jbock.compiler.EvaluatingProcessor;
import net.jbock.compiler.TestExpr;
import net.jbock.compiler.TypeTool;
import org.junit.jupiter.api.Test;

import javax.lang.model.element.TypeElement;

import static net.jbock.compiler.EvaluatingProcessor.assertSameType;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
      TypeElement collectorClass = elements.getTypeElement("ToSetCollector");
      CollectorInfo collectorInfo = CollectorClassValidator.getCollectorInfo(
          TestExpr.parse("java.util.Set<java.lang.String>", elements, types),
          collectorClass,
          new TypeTool(elements, types));
      assertSameType("java.lang.String", collectorInfo.inputType, elements, types);
      assertTrue(collectorInfo.collectorType().isPresent());
      assertSameType("ToSetCollector<java.lang.String>",
          collectorInfo.collectorType().get(), elements, types);
    });
  }
}