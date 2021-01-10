package net.jbock.examples;

import net.jbock.Command;
import net.jbock.Option;

import java.util.OptionalInt;
import java.util.function.Function;
import java.util.function.Supplier;

@Command
abstract class OptionalIntArgumentsOptional {

  @Option(value = "a", mnemonic = 'a', mappedBy = Mapper.class)
  abstract OptionalInt a();

  @net.jbock.Mapper
  static class Mapper implements Supplier<Function<String, Integer>> {

    @Override
    public Function<String, Integer> get() {
      return Integer::parseInt;
    }
  }
}
