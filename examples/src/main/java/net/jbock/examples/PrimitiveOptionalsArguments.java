package net.jbock.examples;

import net.jbock.Command;
import net.jbock.Mapper;
import net.jbock.Option;

import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.function.Function;
import java.util.function.Supplier;

@Command
abstract class PrimitiveOptionalsArguments {

  @Option(names = {"--I", "-I"}, mappedBy = IntegerMapper.class)
  abstract OptionalInt simpleInt();

  @Option(names = {"--L", "-L"}, mappedBy = LongMapper.class)
  abstract OptionalLong simpleLong();

  @Option(names = {"--D", "-D"}, mappedBy = DoubleMapper.class)
  abstract OptionalDouble simpleDouble();

  @Option(names = {"--i", "-i"}, mappedBy = IntegerMapper.class)
  abstract OptionalInt mappedInt();

  @Option(names = {"--l", "-l"}, mappedBy = LongMapper.class)
  abstract OptionalLong mappedLong();

  @Option(names = {"--d", "-d"}, mappedBy = DoubleMapper.class)
  abstract OptionalDouble mappedDouble();

  @Mapper
  static class IntegerMapper implements Supplier<Function<String, Integer>> {
    @Override
    public Function<String, Integer> get() {
      return Integer::valueOf;
    }
  }

  @Mapper
  static class LongMapper implements Supplier<Function<String, Long>> {
    @Override
    public Function<String, Long> get() {
      return Long::valueOf;
    }
  }

  @Mapper
  static class DoubleMapper implements Supplier<Function<String, Double>> {
    @Override
    public Function<String, Double> get() {
      return Double::valueOf;
    }
  }
}
