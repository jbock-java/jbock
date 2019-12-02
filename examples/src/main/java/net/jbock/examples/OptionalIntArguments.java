package net.jbock.examples;

import net.jbock.CLI;
import net.jbock.Option;

import java.util.OptionalInt;
import java.util.function.Function;
import java.util.function.Supplier;

@CLI
abstract class OptionalIntArguments {

  private static final Function<String, Integer> PARSE_INT = Integer::parseInt;

  @Option(value = "a", mnemonic = 'a', mappedBy = Mapper.class)
  abstract OptionalInt a();

  static class Mapper implements Supplier<Function<String, OptionalInt>> {

    @Override
    public Function<String, OptionalInt> get() {
      return PARSE_INT.andThen(OptionalInt::of);
    }
  }
}
