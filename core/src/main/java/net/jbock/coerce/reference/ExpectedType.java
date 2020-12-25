package net.jbock.coerce.reference;

import java.lang.reflect.AnnotatedType;
import java.lang.reflect.ParameterizedType;
import java.util.Locale;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

public class ExpectedType<E> {

  private final String name;
  private final String canonicalName;

  public static final ExpectedType<Collector<?, ?, ?>> COLLECTOR = create("COLLECTOR", collectorClass());

  public static final ExpectedType<Function<?, ?>> MAPPER = create("MAPPER", functionClass());

  private ExpectedType(String name, String canonicalName) {
    this.name = name;
    this.canonicalName = canonicalName;
  }

  private static <E> ExpectedType<E> create(String name, Class<? extends E> expectedClass) {
    return new ExpectedType<>(name, getCanonicalName(expectedClass));
  }

  public String name() {
    return name;
  }

  public String boom(String message) {
    return String.format("There is a problem with the %s class: %s.", name.toLowerCase(Locale.US), message);
  }

  String canonicalName() {
    return canonicalName;
  }

  private static String getCanonicalName(Class<?> expectedClass) {
    AnnotatedType[] interfaces = expectedClass.getAnnotatedInterfaces();
    if (interfaces.length != 1) {
      throw new IllegalArgumentException("Expecting exactly one interface: " + expectedClass);
    }
    AnnotatedType interface1 = interfaces[0];
    if (!(interface1.getType() instanceof ParameterizedType)) {
      throw new AssertionError("Not a parameterized type: " + expectedClass);
    }
    return ((ParameterizedType) interface1.getType()).getRawType().getTypeName();
  }

  private static Class<? extends Function<?, ?>> functionClass() {
    return new Function<String, String>() {
      @Override
      public String apply(String s) {
        return null;
      }
    }.getClass();
  }

  private static Class<? extends Collector<?, ?, ?>> collectorClass() {
    return new Collector<String, String, String>() {
      @Override
      public Supplier<String> supplier() {
        return null;
      }

      @Override
      public BiConsumer<String, String> accumulator() {
        return null;
      }

      @Override
      public BinaryOperator<String> combiner() {
        return null;
      }

      @Override
      public Function<String, String> finisher() {
        return null;
      }

      @Override
      public Set<Characteristics> characteristics() {
        return null;
      }
    }.getClass();
  }
}
