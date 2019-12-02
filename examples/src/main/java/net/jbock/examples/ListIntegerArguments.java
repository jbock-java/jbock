package net.jbock.examples;

import net.jbock.CLI;
import net.jbock.Option;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

@CLI
abstract class ListIntegerArguments {

  private static final Function<String, Integer> PARSE_INT = Integer::parseInt;

  @Option(value = "a", mnemonic = 'a', mappedBy = Mapper.class)
  abstract List<Integer> a();

  static class Mapper implements Supplier<Function<String, List<Integer>>> {

    @Override
    public Function<String, List<Integer>> get() {
      return PARSE_INT.andThen(Collections::singletonList);
    }
  }
}
