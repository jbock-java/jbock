package net.jbock.examples;

import net.jbock.Command;
import net.jbock.Option;
import net.jbock.util.StringConverter;

import java.util.function.Supplier;

@Command
interface PrimitiveArguments {

    @Option(names = {"--B", "-B"})
    byte simpleByte();

    @Option(names = {"--S", "-S"})
    short simpleShort();

    @Option(names = {"--I", "-I"})
    int simpleInt();

    @Option(names = {"--L", "-L"})
    long simpleLong();

    @Option(names = {"--F", "-F"})
    float simpleFloat();

    @Option(names = {"--D", "-D"})
    double simpleDouble();

    @Option(names = {"--C", "-C"})
    char simpleChar();

    // there's no simple boolean -- that would be a flag!

    @Option(names = {"--b", "-b"}, converter = ByteConverter.class)
    byte mappedByte();

    @Option(names = {"--s", "-s"}, converter = ShortConverter.class)
    short mappedShort();

    @Option(names = {"--i", "-i"}, converter = IntConverter.class)
    int mappedInt();

    @Option(names = {"--l", "-l"}, converter = LongConverter.class)
    long mappedLong();

    @Option(names = {"--f", "-f"}, converter = FloatConverter.class)
    float mappedFloat();

    @Option(names = {"--d", "-d"}, converter = DoubleConverter.class)
    double mappedDouble();

    @Option(names = {"--c", "-c"}, converter = CharConverter.class)
    char mappedChar();

    @Option(names = {"--x", "-x"}, converter = BooleanConverter.class)
    boolean mappedBoolean();

    class IntConverter implements Supplier<StringConverter<Integer>> {
        @Override
        public StringConverter<Integer> get() {
            return StringConverter.create(Integer::valueOf);
        }
    }

    class LongConverter implements Supplier<StringConverter<Long>> {
        @Override
        public StringConverter<Long> get() {
            return StringConverter.create(Long::valueOf);
        }
    }

    class DoubleConverter implements Supplier<StringConverter<Double>> {
        @Override
        public StringConverter<Double> get() {
            return StringConverter.create(Double::valueOf);
        }
    }

    class ByteConverter implements Supplier<StringConverter<Byte>> {
        @Override
        public StringConverter<Byte> get() {
            return StringConverter.create(Byte::valueOf);
        }
    }

    class ShortConverter implements Supplier<StringConverter<Short>> {
        @Override
        public StringConverter<Short> get() {
            return StringConverter.create(Short::valueOf);
        }
    }

    class FloatConverter implements Supplier<StringConverter<Float>> {
        @Override
        public StringConverter<Float> get() {
            return StringConverter.create(Float::valueOf);
        }
    }

    class CharConverter implements Supplier<StringConverter<Character>> {
        @Override
        public StringConverter<Character> get() {
            return StringConverter.create(s -> s.charAt(0));
        }
    }

    class BooleanConverter implements Supplier<StringConverter<Boolean>> {
        @Override
        public StringConverter<Boolean> get() {
            return StringConverter.create(Boolean::valueOf);
        }
    }
}
