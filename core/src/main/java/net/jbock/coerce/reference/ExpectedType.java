package net.jbock.coerce.reference;

import java.util.function.Function;
import java.util.stream.Collector;

public class ExpectedType<E> {

  private final String name;
  private final Class<E> expectedClass;

  public static final ExpectedType<Function> MAPPER = new ExpectedType<>("MAPPER", Function.class);
  public static final ExpectedType<Collector> COLLECTOR = new ExpectedType<>("COLLECTOR", Collector.class);

  private ExpectedType(String name, Class<E> expectedClass) {
    this.name = name;
    this.expectedClass = expectedClass;
  }

  public String name() {
    return name;
  }

  public Class<E> expectedClass() {
    return expectedClass;
  }

  public String simpleName() {
    return expectedClass.getSimpleName();
  }
}
