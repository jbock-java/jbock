package net.jbock.examples;

import net.jbock.Command;
import net.jbock.Param;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

@Command
abstract class SudokuArguments {

  @Param(value = 1, mappedBy = Mapper.class)
  abstract java.util.ArrayList<List<List<List<List<List<List<Set<Set<Set<Set<Set<Set<Collection<Integer>>>>>>>>>>>>>> number();

  static class Mapper<M extends Integer> implements Supplier<Function<String, java.util.ArrayList<List<List<List<List<List<List<Set<Set<Set<Set<Set<Set<Collection<M>>>>>>>>>>>>>>>> {
    public Foo1<Set<Set<Set<Set<Set<Set<Collection<M>>>>>>>> get() {
      return s -> new ArrayList<>();
    }
  }

  interface Foo1<A> extends Foo2<List<A>> {
  }

  interface Foo2<B> extends Foo3<List<B>> {
  }

  interface Foo3<C> extends Foo4<List<C>> {
  }

  interface Foo4<D> extends Foo5<List<D>> {
  }

  interface Foo5<E> extends Foo6<List<E>> {
  }

  interface Foo6<F> extends Foo7<List<F>> {
  }

  interface Foo7<G> extends Foo8<java.util.ArrayList<G>> {
  }

  interface Foo8<H> extends Function<String, H> {
  }

}
