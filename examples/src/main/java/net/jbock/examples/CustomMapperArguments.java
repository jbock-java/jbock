package net.jbock.examples;

import net.jbock.CommandLineArguments;
import net.jbock.Parameter;
import net.jbock.PositionalParameter;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@CommandLineArguments
abstract class CustomMapperArguments {

  /**
   * The mapper must be a Function from String to whatever-this-returns.
   * It must also have a package-visible no-arg constructor.
   */
  @Parameter(mappedBy = DateMapper.class)
  abstract Date date();

  @Parameter(mappedBy = DateMapper.class)
  abstract Optional<Date> optDate();

  @Parameter(repeatable = true, mappedBy = DateMapper.class)
  abstract List<Date> dateList();

  @Parameter(mappedBy = CustomBigIntegerMapper.class)
  abstract Optional<BigInteger> verbosity();

  @Parameter(mappedBy = PositiveNumberMapper.class)
  abstract int anInt();

  @Parameter(mappedBy = PositiveNumberMapper.class)
  abstract OptionalInt anOptionalInt();

  @Parameter(mappedBy = ArrayMapper.class)
  abstract Optional<String[]> stringArray();

  @Parameter(mappedBy = IntegerListMapper.class)
  abstract Optional<List<Integer>> integerList();

  @Parameter(mappedBy = EnumSetMapper.class)
  abstract Optional<Set<MyEnum>> enumSet();

  @PositionalParameter(repeatable = true, mappedBy = BooleanMapper.class)
  abstract List<Boolean> booleanList();

  @Parameter(repeatable = true, mappedBy = OptionalIntMapper.class)
  abstract List<OptionalInt> optionalInts();

  @Parameter(mappedBy = BooleanMapper.class)
  abstract boolean notFlag(); // if it has a mapper, it's not a flag

  static class DateMapper implements Supplier<Function<String, Date>> {

    @Override
    public Function<String, Date> get() {
      return s -> new Date(Long.parseLong(s));
    }
  }

  static class PositiveNumberMapper implements Supplier<Function<String, Integer>> {

    @Override
    public Function<String, Integer> get() {
      return s -> {
        Integer i = Integer.valueOf(s);
        if (i < 0) {
          throw new IllegalArgumentException("The value cannot be negative.");
        }
        return i;
      };
    }
  }

  static class ArrayMapper implements Supplier<Function<String, String[]>> {

    @Override
    public Function<String, String[]> get() {
      return s -> new String[]{s};
    }
  }

  static class IntegerListMapper implements Supplier<Function<String, List<Integer>>> {

    @Override
    public Function<String, List<Integer>> get() {
      return s -> Arrays.stream(s.split(",", -1))
          .map(Integer::valueOf)
          .collect(Collectors.toList());
    }
  }

  static class EnumSetMapper implements Supplier<Function<String, Set<MyEnum>>> {

    @Override
    public Function<String, Set<MyEnum>> get() {
      return s -> Arrays.stream(s.split(",", -1))
          .map(MyEnum::valueOf)
          .collect(Collectors.toSet());
    }
  }

  static class BooleanMapper implements Supplier<Function<String, Boolean>> {

    @Override
    public Function<String, Boolean> get() {
      return Boolean::valueOf;
    }
  }

  static class OptionalIntMapper implements Supplier<Function<String, OptionalInt>> {

    @Override
    public Function<String, OptionalInt> get() {
      return s -> {
        if (s.isEmpty()) {
          return OptionalInt.empty();
        }
        return OptionalInt.of(Integer.parseInt(s));
      };
    }
  }

  enum MyEnum {
    FOO, BAR
  }
}
