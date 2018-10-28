package net.jbock.coerce;

import net.jbock.compiler.EvaluatingProcessor;
import org.junit.jupiter.api.Test;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import java.util.Optional;

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
    ).run("ToSetCollector", context -> {
      TypeElement collectorClass = context.elements().getTypeElement("ToSetCollector");
      CollectorInfo collectorInfo = CollectorClassValidator.getCollectorInfo(context.declared("java.util.Set<java.lang.String>"), collectorClass);
      assertEquals(context.types().getDeclaredType(context.elements().getTypeElement("java.lang.String")), collectorInfo.inputType);
      DeclaredType type0 = context.declared("ToSetCollector<java.lang.String>");
      assertEquals(Optional.of(type0), collectorInfo.collectorType());
    });
  }
}