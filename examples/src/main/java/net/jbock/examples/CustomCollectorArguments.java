package net.jbock.examples;

import net.jbock.CommandLineArguments;
import net.jbock.Parameter;

import java.math.BigInteger;
import java.time.LocalDate;
import java.util.AbstractMap;
import java.util.Map;
import java.util.OptionalInt;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@CommandLineArguments
abstract class CustomCollectorArguments {

  @Parameter(repeatable = true, shortName = 'H', collectedBy = MyStringCollector.class)
  abstract Set<String> strings();

  @Parameter(repeatable = true, shortName = 'B', collectedBy = MyIntegerCollector.class)
  abstract Set<Integer> integers();

  @Parameter(
      repeatable = true,
      shortName = 'T',
      mappedBy = MapEntryTokenizer.class,
      collectedBy = ToMapCollector.class)
  abstract Map<String, LocalDate> dateMap();

  @Parameter(
      repeatable = true,
      shortName = 'M',
      mappedBy = CustomBigIntegerMapper.class,
      collectedBy = ToSetCollector.class)
  abstract Set<BigInteger> bigIntegers();

  static class MyStringCollector implements Supplier<Collector<String, ?, Set<String>>> {

    @Override
    public Collector<String, ?, Set<String>> get() {
      return Collectors.toSet();
    }
  }

  static class MapEntryTokenizer implements Supplier<Function<String, Map.Entry<String, LocalDate>>> {

    @Override
    public Function<String, Map.Entry<String, LocalDate>> get() {
      return s -> {
        String[] tokens = s.split(":", 2);
        if (tokens.length < 2) {
          throw new IllegalArgumentException("Invalid pair");
        }
        return new AbstractMap.SimpleImmutableEntry<>(tokens[0], LocalDate.parse(tokens[1]));
      };
    }
  }

  static class ToMapCollector<K, V> implements Supplier<Collector<Map.Entry<K, V>, ?, Map<K, V>>> {

    @Override
    public Collector<Map.Entry<K, V>, ?, Map<K, V>> get() {
      return Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue);
    }
  }

  static class MyIntegerCollector implements Supplier<Collector<Integer, ?, Set<Integer>>> {

    @Override
    public Collector<Integer, ?, Set<Integer>> get() {
      return Collectors.toSet();
    }
  }

  static class ToSetCollector<E> implements Supplier<Collector<E, ?, Set<E>>> {

    @Override
    public Collector<E, ?, Set<E>> get() {
      return Collectors.toSet();
    }
  }
}
