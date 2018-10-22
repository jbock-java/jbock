package net.jbock.examples;

import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;

class ToSetCollector<E> implements Supplier<Collector<E, ?, Set<E>>> {

  @Override
  public Collector<E, ?, Set<E>> get() {
    return Collectors.toSet();
  }
}
