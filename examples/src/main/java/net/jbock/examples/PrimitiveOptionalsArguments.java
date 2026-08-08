package net.jbock.examples;

import net.jbock.Command;
import net.jbock.Option;
import net.jbock.util.StringConverter;

import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.function.Supplier;

@Command
interface PrimitiveOptionalsArguments {

    @Option(names = {"--I", "-I"}, converter = IntegerConverter.class)
    OptionalInt simpleInt();

    @Option(names = {"--L", "-L"}, converter = LongConverter.class)
    OptionalLong simpleLong();

    @Option(names = {"--D", "-D"}, converter = DoubleConverter.class)
    OptionalDouble simpleDouble();

    @Option(names = {"--i", "-i"}, converter = IntegerConverter.class)
    OptionalInt mappedInt();

    @Option(names = {"--l", "-l"}, converter = LongConverter.class)
    OptionalLong mappedLong();

    @Option(names = {"--d", "-d"}, converter = DoubleConverter.class)
    OptionalDouble mappedDouble();

    class IntegerConverter implements Supplier<StringConverter<Integer>> {
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
}
