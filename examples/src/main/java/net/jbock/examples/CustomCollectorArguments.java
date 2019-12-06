package net.jbock.examples;

import net.jbock.Command;
import net.jbock.Option;

import java.math.BigInteger;
import java.time.LocalDate;
import java.util.AbstractMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@Command
abstract class CustomCollectorArguments {

  @Option(value = "H", mnemonic = 'H', collectedBy = MyStringCollector.class)
  abstract Set<String> strings();

  @Option(value = "B", mnemonic = 'B', collectedBy = MyIntegerCollector.class)
  abstract Set<Integer> integers();

  @Option(value = "K", mnemonic = 'K', collectedBy = ToSetCollector.class)
  abstract Set<Giddy> moneySet();

  @Option(
      value = "T",
      mnemonic = 'T',
      mappedBy = MapEntryTokenizer.class,
      collectedBy = ToMapCollector.class)
  abstract Map<String, LocalDate> dateMap();

  @Option(
      value = "M",
      mnemonic = 'M',
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

  enum Giddy {
    SOME, NONE
  }

  static class MyIntegerCollector implements Supplier<Collector<Integer, ?, Set<Integer>>> {

    @Override
    public Collector<Integer, ?, Set<Integer>> get() {
      return Collectors.toSet();
    }
  }

}
