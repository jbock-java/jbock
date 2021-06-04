package net.jbock.examples;

import net.jbock.Command;
import net.jbock.Converter;
import net.jbock.Option;
import net.jbock.util.StringConverter;

import java.util.OptionalInt;
import java.util.function.Function;
import java.util.function.Supplier;

@Command
abstract class OptionalIntArguments {

  private static final Function<String, Integer> PARSE_INT = Integer::parseInt;

  @Option(names = {"--a", "-a"}, converter = Mapper.class)
  abstract OptionalInt a();

  @Converter
  static class Mapper implements Supplier<StringConverter<OptionalInt>> {

    @Override
    public StringConverter<OptionalInt> get() {
      return StringConverter.create(PARSE_INT.andThen(OptionalInt::of));
    }
  }
}
