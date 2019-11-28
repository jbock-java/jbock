package net.jbock.examples;

import net.jbock.CommandLineArguments;
import net.jbock.Parameter;

import java.util.function.Function;
import java.util.function.Supplier;

@CommandLineArguments
abstract class PrimitiveArguments {

  @Parameter(value = "B", mnemonic = 'B')
  abstract byte simpleByte();

  @Parameter(value = "S", mnemonic = 'S')
  abstract short simpleShort();

  @Parameter(value = "I", mnemonic = 'I')
  abstract int simpleInt();

  @Parameter(value = "L", mnemonic = 'L')
  abstract long simpleLong();

  @Parameter(value = "F", mnemonic = 'F')
  abstract float simpleFloat();

  @Parameter(value = "D", mnemonic = 'D')
  abstract double simpleDouble();

  @Parameter(value = "C", mnemonic = 'C')
  abstract char simpleChar();

  // there's no simple boolean -- that would be a flag!

  @Parameter(value = "b", mnemonic = 'b', mappedBy = ByteMapper.class)
  abstract byte mappedByte();

  @Parameter(value = "s", mnemonic = 's', mappedBy = ShortMapper.class)
  abstract short mappedShort();

  @Parameter(value = "i", mnemonic = 'i', mappedBy = IntMapper.class)
  abstract int mappedInt();

  @Parameter(value = "l", mnemonic = 'l', mappedBy = LongMapper.class)
  abstract long mappedLong();

  @Parameter(value = "f", mnemonic = 'f', mappedBy = FloatMapper.class)
  abstract float mappedFloat();

  @Parameter(value = "d", mnemonic = 'd', mappedBy = DoubleMapper.class)
  abstract double mappedDouble();

  @Parameter(value = "c", mnemonic = 'c', mappedBy = CharMapper.class)
  abstract char mappedChar();

  @Parameter(value = "x", mnemonic = 'x', mappedBy = BooleanMapper.class)
  abstract boolean mappedBoolean();

  static class IntMapper implements Supplier<Function<String, Integer>> {
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

  static class ByteMapper implements Supplier<Function<String, Byte>> {
    @Override
    public Function<String, Byte> get() {
      return Byte::valueOf;
    }
  }

  static class ShortMapper implements Supplier<Function<String, Short>> {
    @Override
    public Function<String, Short> get() {
      return Short::valueOf;
    }
  }

  static class FloatMapper implements Supplier<Function<String, Float>> {
    @Override
    public Function<String, Float> get() {
      return Float::valueOf;
    }
  }

  static class CharMapper implements Supplier<Function<String, Character>> {
    @Override
    public Function<String, Character> get() {
      return s -> s.charAt(0);
    }
  }

  static class BooleanMapper implements Supplier<Function<String, Boolean>> {
    @Override
    public Function<String, Boolean> get() {
      return Boolean::valueOf;
    }
  }
}
