package net.jbock.examples;

import net.jbock.Command;
import net.jbock.Mapper;
import net.jbock.Option;

import java.util.function.Function;
import java.util.function.Supplier;

@Command
abstract class PrimitiveArguments {

  @Option(value = "B", mnemonic = 'B')
  abstract byte simpleByte();

  @Option(value = "S", mnemonic = 'S')
  abstract short simpleShort();

  @Option(value = "I", mnemonic = 'I')
  abstract int simpleInt();

  @Option(value = "L", mnemonic = 'L')
  abstract long simpleLong();

  @Option(value = "F", mnemonic = 'F')
  abstract float simpleFloat();

  @Option(value = "D", mnemonic = 'D')
  abstract double simpleDouble();

  @Option(value = "C", mnemonic = 'C')
  abstract char simpleChar();

  // there's no simple boolean -- that would be a flag!

  @Option(value = "b", mnemonic = 'b', mappedBy = ByteMapper.class)
  abstract byte mappedByte();

  @Option(value = "s", mnemonic = 's', mappedBy = ShortMapper.class)
  abstract short mappedShort();

  @Option(value = "i", mnemonic = 'i', mappedBy = IntMapper.class)
  abstract int mappedInt();

  @Option(value = "l", mnemonic = 'l', mappedBy = LongMapper.class)
  abstract long mappedLong();

  @Option(value = "f", mnemonic = 'f', mappedBy = FloatMapper.class)
  abstract float mappedFloat();

  @Option(value = "d", mnemonic = 'd', mappedBy = DoubleMapper.class)
  abstract double mappedDouble();

  @Option(value = "c", mnemonic = 'c', mappedBy = CharMapper.class)
  abstract char mappedChar();

  @Option(value = "x", mnemonic = 'x', mappedBy = BooleanMapper.class)
  abstract boolean mappedBoolean();

  @Mapper
  static class IntMapper implements Supplier<Function<String, Integer>> {
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

  @Mapper
  static class ByteMapper implements Supplier<Function<String, Byte>> {
    @Override
    public Function<String, Byte> get() {
      return Byte::valueOf;
    }
  }

  @Mapper
  static class ShortMapper implements Supplier<Function<String, Short>> {
    @Override
    public Function<String, Short> get() {
      return Short::valueOf;
    }
  }

  @Mapper
  static class FloatMapper implements Supplier<Function<String, Float>> {
    @Override
    public Function<String, Float> get() {
      return Float::valueOf;
    }
  }

  @Mapper
  static class CharMapper implements Supplier<Function<String, Character>> {
    @Override
    public Function<String, Character> get() {
      return s -> s.charAt(0);
    }
  }

  @Mapper
  static class BooleanMapper implements Supplier<Function<String, Boolean>> {
    @Override
    public Function<String, Boolean> get() {
      return Boolean::valueOf;
    }
  }
}
