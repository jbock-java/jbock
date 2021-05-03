package net.jbock.examples;

import net.jbock.Command;
import net.jbock.Mapper;
import net.jbock.Option;

import java.util.OptionalInt;
import java.util.function.Function;
import java.util.function.Supplier;

@Command
abstract class OptionalIntArgumentsOptional {

  @Option(names = {"--a", "-a"}, mappedBy = MyMapper.class)
  abstract OptionalInt a();

  @Mapper
  static class MyMapper implements Supplier<Function<String, Integer>> {

    @Override
    public Function<String, Integer> get() {
      return Integer::parseInt;
    }
  }
}
