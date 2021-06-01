package net.jbock.examples;

import net.jbock.Command;
import net.jbock.Converter;
import net.jbock.Option;
import net.jbock.StringConverter;

import java.util.OptionalInt;
import java.util.function.Supplier;

@Command
abstract class OptionalIntArgumentsOptional {

  @Option(names = {"--a", "-a"}, converter = MyConverter.class)
  abstract OptionalInt a();

  @Converter
  static class MyConverter implements Supplier<StringConverter<Integer>> {

    @Override
    public StringConverter<Integer> get() {
      return StringConverter.create(Integer::parseInt);
    }
  }
}
