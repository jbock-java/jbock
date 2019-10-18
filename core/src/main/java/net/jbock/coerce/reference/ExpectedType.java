package net.jbock.coerce.reference;

import java.util.function.Function;
import java.util.stream.Collector;

public enum ExpectedType {

  MAPPER(Function.class),
  COLLECTOR(Collector.class);

  private final Class<?> expectedClass;

  ExpectedType(Class<?> expectedClass) {
    this.expectedClass = expectedClass;
  }

  Class<?> expectedClass() {
    return expectedClass;
  }
}
