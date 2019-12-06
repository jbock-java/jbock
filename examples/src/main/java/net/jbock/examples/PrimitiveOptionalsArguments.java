package net.jbock.examples;

import net.jbock.Command;
import net.jbock.Option;

import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.function.Function;
import java.util.function.Supplier;

@Command
abstract class PrimitiveOptionalsArguments {

  @Option(value = "I", mnemonic = 'I', mappedBy = IntegerMapper.class)
  abstract OptionalInt simpleInt();

  @Option(value = "L", mnemonic = 'L', mappedBy = LongMapper.class)
  abstract OptionalLong simpleLong();

  @Option(value = "D", mnemonic = 'D', mappedBy = DoubleMapper.class)
  abstract OptionalDouble simpleDouble();

  @Option(value = "i", mnemonic = 'i', mappedBy = IntegerMapper.class)
  abstract OptionalInt mappedInt();

  @Option(value = "l", mnemonic = 'l', mappedBy = LongMapper.class)
  abstract OptionalLong mappedLong();

  @Option(value = "d", mnemonic = 'd', mappedBy = DoubleMapper.class)
  abstract OptionalDouble mappedDouble();

  static class IntegerMapper implements Supplier<Function<String, Integer>> {
    @Override
    public Function<String, Integer> get() {
      return Integer::valueOf;
    }
  }

  static class LongMapper implements Supplier<Function<String, Long>> {
    @Override
    public Function<String, Long> get() {
      return Long::valueOf;
    }
  }

  static class DoubleMapper implements Supplier<Function<String, Double>> {
    @Override
    public Function<String, Double> get() {
      return Double::valueOf;
    }
  }
}
