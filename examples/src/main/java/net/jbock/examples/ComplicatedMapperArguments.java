package net.jbock.examples;

import net.jbock.Command;
import net.jbock.Option;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

@Command
abstract class ComplicatedMapperArguments {

  @Option(
      value = "number",
      mapper = Mapper.class)
  abstract Integer number();

  @Option(
      value = "numbers",
      mapper = LazyNumberMapper.class)
  abstract List<LazyNumber> numbers();

  @net.jbock.Mapper
  static class LazyNumberMapper implements Supplier<Function<String, LazyNumber>> {
    @Override
    public Function<String, LazyNumber> get() {
      return s -> () -> Integer.valueOf(s);
    }
  }

  interface LazyNumber extends Supplier<Integer> {
  }

  // parser must understand that this implements Function<String, Integer>
  @net.jbock.Mapper
  static class Mapper implements Supplier<Function<String, Integer>> {
    @Override
    public Function<String, Integer> get() {
      return new Zapper();
    }
  }

  static class Zapper implements Foo<String> {
    public Integer apply(String s) {
      return 1;
    }
  }

  interface Xi<A, T, B> extends Function<B, A> {
  }

  interface Zap<T, B, A> extends Xi<A, T, B> {
  }

  interface Foo<X> extends Zap<X, String, Integer> {
  }
}
