package net.jbock.examples;

import net.jbock.CommandLineArguments;
import net.jbock.Parameter;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

@CommandLineArguments
abstract class OptionalIntegerArguments {

  private static final Function<String, Integer> PARSE_INT = Integer::parseInt;

  @Parameter(value = "a", mnemonic = 'a', mappedBy = Mapper.class)
  abstract Optional<Integer> a();

  static class Mapper implements Supplier<Function<String, Optional<Integer>>> {

    @Override
    public Function<String, Optional<Integer>> get() {
      return PARSE_INT.andThen(Optional::of);
    }
  }
}
