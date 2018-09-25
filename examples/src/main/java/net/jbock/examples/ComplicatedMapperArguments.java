package net.jbock.examples;

import net.jbock.CommandLineArguments;
import net.jbock.Parameter;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

@CommandLineArguments
abstract class ComplicatedMapperArguments {

  @Parameter(mappedBy = Mapper.class)
  abstract Integer number();

  @Parameter(repeatable = true, mappedBy = LazyNumberMapper.class)
  abstract List<LazyNumber> numbers();

  static class LazyNumberMapper implements Supplier<Function<String, LazyNumber>> {
    @Override
    public Function<String, LazyNumber> get() {
      return s -> () -> Integer.valueOf(s);
    }
  }

  interface LazyNumber extends Supplier<Integer> {
  }

  // parser must understand that this implements Function<String, Integer>
  static class Mapper implements ZapperSupplier {
    @Override
    public Zapper get() {
      return new Zapper();
    }
  }

  interface ZapperSupplier extends Supplier<Zapper> {
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
