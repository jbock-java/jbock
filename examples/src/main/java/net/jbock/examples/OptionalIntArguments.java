package net.jbock.examples;

import net.jbock.CommandLineArguments;
import net.jbock.Parameter;

import java.util.OptionalInt;
import java.util.function.Function;
import java.util.function.Supplier;

@CommandLineArguments
abstract class OptionalIntArguments {

  private static final Function<String, Integer> PARSE_INT = Integer::parseInt;

  @Parameter(shortName = 'a', mappedBy = Mapper.class)
  abstract OptionalInt a();

  static class Mapper implements Supplier<Function<String, OptionalInt>> {

    @Override
    public Function<String, OptionalInt> get() {
      return PARSE_INT.andThen(OptionalInt::of);
    }
  }
}
