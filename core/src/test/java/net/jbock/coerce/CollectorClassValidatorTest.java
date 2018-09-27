package net.jbock.coerce;

import org.junit.jupiter.api.Test;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

class CollectorClassValidatorTest {

  @Test
  void runTests() {

    EvaluatingProcessor.builder("ToSetCollector").source(
        "import java.util.Set;",
        "import java.util.function.Supplier;",
        "import java.util.stream.Collector;",
        "import java.util.stream.Collectors;",
        "",
        "abstract class SimpleCollector implements Supplier<Collector<String, ?, Set<String>>> {}",
        "abstract class ToSetCollector<E> implements Supplier<Collector<E, ?, Set<E>>> {}"
    ).run((elements, types) -> {
      TypeElement parameterized = elements.getTypeElement("ToSetCollector");
      TypeElement simple = elements.getTypeElement("SimpleCollector");
      TypeMirror supplierInterface = parameterized.getInterfaces().get(0);

      System.out.println(types.isAssignable(supplierInterface, simple.asType()));
      System.out.println(types.isAssignable(simple.asType(), supplierInterface));
    });
  }
}