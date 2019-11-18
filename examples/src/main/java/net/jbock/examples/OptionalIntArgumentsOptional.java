package net.jbock.examples;

import net.jbock.CommandLineArguments;
import net.jbock.Parameter;

import java.util.OptionalInt;
import java.util.function.Function;
import java.util.function.Supplier;

@CommandLineArguments
abstract class OptionalIntArgumentsOptional {

  @Parameter(value = "a", mnemonic = 'a', mappedBy = Mapper.class)
  abstract OptionalInt a();

  static class Mapper implements Supplier<Function<String, Integer>> {

    @Override
    public Function<String, Integer> get() {
      return Integer::parseInt;
    }
  }
}
