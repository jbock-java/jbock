package net.jbock.coerce;

import net.jbock.compiler.EvaluatingProcessor;
import org.junit.jupiter.api.Test;

import javax.lang.model.element.TypeElement;

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
    ).run("ToSetCollector", context -> {
      TypeElement collectorClass = context.elements().getTypeElement("ToSetCollector");
      CollectorInfo collectorInfo = CollectorClassValidator.getCollectorInfo(
          context.declared("java.util.Set<java.lang.String>"), collectorClass);
      context.assertSameType("java.lang.String", collectorInfo.inputType);
      assertTrue(collectorInfo.collectorType().isPresent());
      context.assertSameType("ToSetCollector<java.lang.String>",
          collectorInfo.collectorType().get());
    });
  }
}