package net.jbock.coerce.reference;

import java.util.Locale;
import java.util.function.Function;
import java.util.stream.Collector;

public class ExpectedType<E> {

  private final String name;
  private final Class<E> expectedClass;

  @SuppressWarnings("rawtypes")
  public static final ExpectedType<Collector> COLLECTOR = new ExpectedType<>("COLLECTOR", Collector.class);

  @SuppressWarnings("rawtypes")
  public static final ExpectedType<Function> FUNCTION = new ExpectedType<>("MAPPER", Function.class);

  private ExpectedType(String name, Class<E> expectedClass) {
    this.name = name;
    this.expectedClass = expectedClass;
  }

  public String name() {
    return name;
  }

  public String boom(String message) {
    return String.format("There is a problem with the %s class: %s.", name.toLowerCase(Locale.US), message);
  }

  Class<E> expectedClass() {
    return expectedClass;
  }

  String simpleName() {
    return expectedClass.getSimpleName();
  }
}
