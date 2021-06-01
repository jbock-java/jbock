package net.jbock.examples;

import net.jbock.Command;
import net.jbock.Converter;
import net.jbock.Option;
import net.jbock.StringConverter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.function.Function;
import java.util.function.Supplier;

@Command
abstract class ListIntegerArguments {

  private static final Function<String, Integer> PARSE_INT = Integer::parseInt;

  @Option(names = {"--a", "-a"}, converter = Mapper.class)
  abstract ArrayList<Integer> a();

  @Converter
  static class Mapper implements Supplier<StringConverter<ArrayList<Integer>>> {

    @Override
    public StringConverter<ArrayList<Integer>> get() {
      return StringConverter.create(PARSE_INT.andThen(Collections::singletonList).andThen(ArrayList::new));
    }
  }
}
