package net.jbock.examples;

import net.jbock.CLI;
import net.jbock.Option;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

@CLI
abstract class OptionalIntegerArguments {

  private static final Function<String, Integer> PARSE_INT = Integer::parseInt;

  @Option(value = "a", mnemonic = 'a', mappedBy = Mapper.class)
  abstract Optional<Integer> a();

  static class Mapper implements Supplier<Function<String, Optional<Integer>>> {

    @Override
    public Function<String, Optional<Integer>> get() {
      return PARSE_INT.andThen(Optional::of);
    }
  }
}
