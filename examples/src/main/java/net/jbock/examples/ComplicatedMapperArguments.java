package net.jbock.examples;

import net.jbock.CommandLineArguments;
import net.jbock.Parameter;

import java.util.function.Function;

@CommandLineArguments
abstract class ComplicatedMapperArguments {

  @Parameter(mappedBy = Mapper.class)
  abstract Integer number();

  // parser must understand that this implements Function<String, Integer>
  static class Mapper implements Foo<String>, Xoxo<Integer> {
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

  interface Bar<E extends Number> extends Function<String, E> {
  }

  interface Xoxo<X extends Number> extends Bar<X> {
  }
}
