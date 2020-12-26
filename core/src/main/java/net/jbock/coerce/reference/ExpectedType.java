package net.jbock.coerce.reference;

import java.lang.reflect.AnnotatedType;
import java.lang.reflect.ParameterizedType;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

public class ExpectedType<E> {

  private final String canonicalName;

  public static final ExpectedType<Collector<?, ?, ?>> COLLECTOR = create(collectorClass());

  public static final ExpectedType<Function<?, ?>> MAPPER = create(functionClass());

  private ExpectedType(String canonicalName) {
    this.canonicalName = canonicalName;
  }

  private static <E> ExpectedType<E> create(Class<? extends E> expectedClass) {
    return new ExpectedType<>(getCanonicalName(expectedClass));
  }

  String canonicalName() {
    return canonicalName;
  }

  private static String getCanonicalName(Class<?> expectedClass) {
    AnnotatedType[] interfaces = expectedClass.getAnnotatedInterfaces();
    if (interfaces.length != 1) {
      throw new AssertionError("Expecting exactly one interface: " + expectedClass);
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
