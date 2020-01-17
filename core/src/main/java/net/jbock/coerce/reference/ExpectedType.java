package net.jbock.coerce.reference;

import java.util.function.Function;
import java.util.stream.Collector;

public abstract class ExpectedType<E> {

  private final String name;
  private final Class<E> expectedClass;

  public static final ExpectedType<Function> FUNCTION = new ExpectedType<Function>("MAPPER", Function.class) {
    @Override
    public String boom(String message) {
      return String.format("There is a problem with the mapper class: %s.", message);
    }
  };
  public static final ExpectedType<Collector> COLLECTOR = new ExpectedType<Collector>("COLLECTOR", Collector.class) {
    @Override
    public String boom(String message) {
      return String.format("There is a problem with the collector class: %s.", message);
    }
  };

  private ExpectedType(String name, Class<E> expectedClass) {
    this.name = name;
    this.expectedClass = expectedClass;
  }

  public String name() {
    return name;
  }

  public abstract String boom(String message);

  Class<E> expectedClass() {
    return expectedClass;
  }

  String simpleName() {
    return expectedClass.getSimpleName();
  }
}
