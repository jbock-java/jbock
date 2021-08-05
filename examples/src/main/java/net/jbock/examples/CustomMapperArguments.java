package net.jbock.examples;

import net.jbock.Command;
import net.jbock.Option;
import net.jbock.Parameters;
import net.jbock.util.StringConverter;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Command
abstract class CustomMapperArguments {

    /**
     * The mapper must be a Function from String to whatever-this-returns.
     * It must also have a package-visible no-arg constructor.
     */
    @Option(names = "--date", converter = DateConverter.class)
    abstract Date date();

    @Option(names = "--optDate", converter = DateConverter.class)
    abstract Optional<Date> optDate();

    @Option(names = "--dateList", converter = DateConverter.class)
    abstract List<Date> dateList();

    @Option(names = "--verbosity", converter = MapMap.class)
    abstract Optional<BigInteger> verbosity();

    @Option(names = "--aRequiredInt", converter = PositiveNumberConverter.class)
    abstract int aRequiredInt();

    @Option(names = "--stringArray", converter = ArrayConverter.class)
    abstract Optional<String[]> stringArray();

    @Option(names = "--integerList", converter = IntegerListConverter.class)
    abstract Optional<ArrayList<Integer>> integerList();

    @Option(names = "--enumSet", converter = EnumSetConverter.class)
    abstract Optional<Set<MyEnum>> enumSet();

    @Parameters(converter = BooleanConverter.class)
    abstract List<Boolean> booleanList();

    @Option(names = "--optionalInts", converter = OptionalIntMapper.class)
    abstract List<OptionalInt> optionalInts();

    @Option(names = "--listWrapper", converter = ListWrapperConverter.class)
    abstract Optional<ArrayList<String>> listWrapper();

    @Option(names = "--notFlag", converter = BooleanConverter.class)
    abstract Boolean notFlag();

    static class DateConverter implements Supplier<StringConverter<Date>> {

        @Override
        public StringConverter<Date> get() {
            return StringConverter.create(s -> new Date(Long.parseLong(s)));
        }
    }

    static class PositiveNumberConverter implements Supplier<StringConverter<Integer>> {

        @Override
        public StringConverter<Integer> get() {
            return StringConverter.create(s -> {
                int i = Integer.parseInt(s);
                if (i < 0) {
                    throw new IllegalArgumentException("The value cannot be negative.");
                }
                return i;
            });
        }
    }

    static class ArrayConverter implements Supplier<StringConverter<String[]>> {

        @Override
        public StringConverter<String[]> get() {
            return StringConverter.create(s -> new String[]{s});
        }
    }

    static class IntegerListConverter implements Supplier<StringConverter<ArrayList<Integer>>> {

        @Override
        public StringConverter<ArrayList<Integer>> get() {
            return StringConverter.create(s -> new ArrayList<>(Arrays.stream(s.split(",", -1))
                    .map(Integer::valueOf)
                    .collect(Collectors.toList())));
        }
    }

    static class EnumSetConverter implements Supplier<StringConverter<Set<MyEnum>>> {

        @Override
        public StringConverter<Set<MyEnum>> get() {
            return StringConverter.create(s -> Arrays.stream(s.split(",", -1))
                    .map(MyEnum::valueOf)
                    .collect(Collectors.toSet()));
        }
    }

    static class BooleanConverter implements Supplier<StringConverter<Boolean>> {

        @Override
        public StringConverter<Boolean> get() {
            return StringConverter.create(Boolean::valueOf);
        }
    }

    static class ListWrapperConverter implements Supplier<StringConverter<ArrayList<String>>> {

        @Override
        public StringConverter<ArrayList<String>> get() {
            return StringConverter.create(s -> new ArrayList<>(Collections.singletonList(s)));
        }
    }

    static class OptionalIntMapper implements Supplier<StringConverter<OptionalInt>> {

        @Override
        public StringConverter<OptionalInt> get() {
            return StringConverter.create(s -> {
                if (s.isEmpty()) {
                    return OptionalInt.empty();
                }
                return OptionalInt.of(Integer.parseInt(s));
            });
        }
    }

    enum MyEnum {
        FOO, BAR
    }

    static class MapMap implements Supplier<StringConverter<BigInteger>> {

        @Override
        public StringConverter<BigInteger> get() {
            return new CustomBigIntegerMapperSupplier();
        }
    }
}
