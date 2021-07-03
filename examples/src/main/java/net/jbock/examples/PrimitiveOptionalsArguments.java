package net.jbock.examples;

import net.jbock.Command;
import net.jbock.Converter;
import net.jbock.Option;
import net.jbock.util.StringConverter;

import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.function.Supplier;

@Command
abstract class PrimitiveOptionalsArguments {

    @Option(names = {"--I", "-I"}, converter = IntegerConverter.class)
    abstract OptionalInt simpleInt();

    @Option(names = {"--L", "-L"}, converter = LongConverter.class)
    abstract OptionalLong simpleLong();

    @Option(names = {"--D", "-D"}, converter = DoubleConverter.class)
    abstract OptionalDouble simpleDouble();

    @Option(names = {"--i", "-i"}, converter = IntegerConverter.class)
    abstract OptionalInt mappedInt();

    @Option(names = {"--l", "-l"}, converter = LongConverter.class)
    abstract OptionalLong mappedLong();

    @Option(names = {"--d", "-d"}, converter = DoubleConverter.class)
    abstract OptionalDouble mappedDouble();

    @Converter
    static class IntegerConverter implements Supplier<StringConverter<Integer>> {
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
}
