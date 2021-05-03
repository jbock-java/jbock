package net.jbock.examples;

import net.jbock.Command;
import net.jbock.Mapper;
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

  @Option(names = {"--b", "-b"}, mappedBy = ByteMapper.class)
  abstract byte mappedByte();

  @Option(names = {"--s", "-s"}, mappedBy = ShortMapper.class)
  abstract short mappedShort();

  @Option(names = {"--i", "-i"}, mappedBy = IntMapper.class)
  abstract int mappedInt();

  @Option(names = {"--l", "-l"}, mappedBy = LongMapper.class)
  abstract long mappedLong();

  @Option(names = {"--f", "-f"}, mappedBy = FloatMapper.class)
  abstract float mappedFloat();

  @Option(names = {"--d", "-d"}, mappedBy = DoubleMapper.class)
  abstract double mappedDouble();

  @Option(names = {"--c", "-c"}, mappedBy = CharMapper.class)
  abstract char mappedChar();

  @Option(names = {"--x", "-x"}, mappedBy = BooleanMapper.class)
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
