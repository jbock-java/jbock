package net.jbock.examples;

import net.jbock.Command;
import net.jbock.Converter;
import net.jbock.Option;

import java.util.function.Function;
import java.util.function.Supplier;

@Command
abstract class PrimitiveArguments {

  @Option(names = {"--B", "-B"})
  abstract byte simpleByte();

  @Option(names = {"--S", "-S"})
  abstract short simpleShort();

  @Option(names = {"--I", "-I"})
  abstract int simpleInt();

  @Option(names = {"--L", "-L"})
  abstract long simpleLong();

  @Option(names = {"--F", "-F"})
  abstract float simpleFloat();

  @Option(names = {"--D", "-D"})
  abstract double simpleDouble();

  @Option(names = {"--C", "-C"})
  abstract char simpleChar();

  // there's no simple boolean -- that would be a flag!

  @Option(names = {"--b", "-b"}, converter = ByteMapper.class)
  abstract byte mappedByte();

  @Option(names = {"--s", "-s"}, converter = ShortMapper.class)
  abstract short mappedShort();

  @Option(names = {"--i", "-i"}, converter = IntMapper.class)
  abstract int mappedInt();

  @Option(names = {"--l", "-l"}, converter = LongMapper.class)
  abstract long mappedLong();

  @Option(names = {"--f", "-f"}, converter = FloatMapper.class)
  abstract float mappedFloat();

  @Option(names = {"--d", "-d"}, converter = DoubleMapper.class)
  abstract double mappedDouble();

  @Option(names = {"--c", "-c"}, converter = CharMapper.class)
  abstract char mappedChar();

  @Option(names = {"--x", "-x"}, converter = BooleanMapper.class)
  abstract boolean mappedBoolean();

  @Converter
  static class IntMapper implements Supplier<Function<String, Integer>> {
    @Override
    public Function<String, Integer> get() {
      return Integer::valueOf;
    }
  }

  @Converter
  static class LongMapper implements Supplier<Function<String, Long>> {
    @Override
    public Function<String, Long> get() {
      return Long::valueOf;
    }
  }

  @Converter
  static class DoubleMapper implements Supplier<Function<String, Double>> {
    @Override
    public Function<String, Double> get() {
      return Double::valueOf;
    }
  }

  @Converter
  static class ByteMapper implements Supplier<Function<String, Byte>> {
    @Override
    public Function<String, Byte> get() {
      return Byte::valueOf;
    }
  }

  @Converter
  static class ShortMapper implements Supplier<Function<String, Short>> {
    @Override
    public Function<String, Short> get() {
      return Short::valueOf;
    }
  }

  @Converter
  static class FloatMapper implements Supplier<Function<String, Float>> {
    @Override
    public Function<String, Float> get() {
      return Float::valueOf;
    }
  }

  @Converter
  static class CharMapper implements Supplier<Function<String, Character>> {
    @Override
    public Function<String, Character> get() {
      return s -> s.charAt(0);
    }
  }

  @Converter
  static class BooleanMapper implements Supplier<Function<String, Boolean>> {
    @Override
    public Function<String, Boolean> get() {
      return Boolean::valueOf;
    }
  }
}
