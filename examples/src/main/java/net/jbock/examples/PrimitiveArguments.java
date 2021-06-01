package net.jbock.examples;

import net.jbock.Command;
import net.jbock.Converter;
import net.jbock.Option;
import net.jbock.StringConverter;

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

  @Option(names = {"--b", "-b"}, converter = ByteConverter.class)
  abstract byte mappedByte();

  @Option(names = {"--s", "-s"}, converter = ShortConverter.class)
  abstract short mappedShort();

  @Option(names = {"--i", "-i"}, converter = IntConverter.class)
  abstract int mappedInt();

  @Option(names = {"--l", "-l"}, converter = LongConverter.class)
  abstract long mappedLong();

  @Option(names = {"--f", "-f"}, converter = FloatConverter.class)
  abstract float mappedFloat();

  @Option(names = {"--d", "-d"}, converter = DoubleConverter.class)
  abstract double mappedDouble();

  @Option(names = {"--c", "-c"}, converter = CharConverter.class)
  abstract char mappedChar();

  @Option(names = {"--x", "-x"}, converter = BooleanConverter.class)
  abstract boolean mappedBoolean();

  @Converter
  static class IntConverter implements Supplier<StringConverter<Integer>> {
    @Override
    public StringConverter<Integer> get() {
      return StringConverter.create(Integer::valueOf);
    }
  }

  @Converter
  static class LongConverter implements Supplier<StringConverter<Long>> {
    @Override
    public StringConverter<Long> get() {
      return StringConverter.create(Long::valueOf);
    }
  }

  @Converter
  static class DoubleConverter implements Supplier<StringConverter<Double>> {
    @Override
    public StringConverter<Double> get() {
      return StringConverter.create(Double::valueOf);
    }
  }

  @Converter
  static class ByteConverter implements Supplier<StringConverter<Byte>> {
    @Override
    public StringConverter<Byte> get() {
      return StringConverter.create(Byte::valueOf);
    }
  }

  @Converter
  static class ShortConverter implements Supplier<StringConverter<Short>> {
    @Override
    public StringConverter<Short> get() {
      return StringConverter.create(Short::valueOf);
    }
  }

  @Converter
  static class FloatConverter implements Supplier<StringConverter<Float>> {
    @Override
    public StringConverter<Float> get() {
      return StringConverter.create(Float::valueOf);
    }
  }

  @Converter
  static class CharConverter implements Supplier<StringConverter<Character>> {
    @Override
    public StringConverter<Character> get() {
      return StringConverter.create(s -> s.charAt(0));
    }
  }

  @Converter
  static class BooleanConverter implements Supplier<StringConverter<Boolean>> {
    @Override
    public StringConverter<Boolean> get() {
      return StringConverter.create(Boolean::valueOf);
    }
  }
}
