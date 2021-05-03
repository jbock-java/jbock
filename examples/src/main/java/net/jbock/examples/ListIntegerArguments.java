package net.jbock.examples;

import net.jbock.Command;
import net.jbock.Converter;
import net.jbock.Option;

import java.util.ArrayList;
import java.util.Collections;
import java.util.function.Function;
import java.util.function.Supplier;

@Command
abstract class ListIntegerArguments {

  private static final Function<String, Integer> PARSE_INT = Integer::parseInt;

  @Option(names = {"--a", "-a"}, converter = Mapper.class)
  abstract java.util.ArrayList<Integer> a();

  @Converter
  static class Mapper implements Supplier<Function<String, java.util.ArrayList<Integer>>> {

    @Override
    public Function<String, java.util.ArrayList<Integer>> get() {
      return PARSE_INT.andThen(Collections::singletonList).andThen(ArrayList::new);
    }
  }
}
