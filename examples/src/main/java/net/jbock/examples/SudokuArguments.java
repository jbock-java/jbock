package net.jbock.examples;

import net.jbock.CommandLineArguments;
import net.jbock.PositionalParameter;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

@CommandLineArguments
abstract class SudokuArguments {

  @PositionalParameter(mappedBy = Mapper.class)
  abstract List<List<List<List<List<List<List<Set<Set<Set<Set<Set<Set<Collection<Integer>>>>>>>>>>>>>> number();

  static class Mapper<M extends Integer> implements Supplier<Function<String, List<List<List<List<List<List<List<Set<Set<Set<Set<Set<Set<Collection<M>>>>>>>>>>>>>>>> {
    public Foo1<Set<Set<Set<Set<Set<Set<Collection<M>>>>>>>> get() {
      return s -> Collections.emptyList();
    }
  }

  //@formatter:off
  interface Foo1<A> extends Foo2<List<A>> { }
  interface Foo2<B> extends Foo3<List<B>> { }
  interface Foo3<C> extends Foo4<List<C>> { }
  interface Foo4<D> extends Foo5<List<D>> { }
  interface Foo5<E> extends Foo6<List<E>> { }
  interface Foo6<F> extends Foo7<List<F>> { }
  interface Foo7<G> extends Foo8<List<G>> { }
  interface Foo8<H> extends Function<String, H> { }
  //@formatter:on

}
