package net.jbock.examples;

import net.jbock.CommandLineArguments;
import net.jbock.Parameter;

import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@CommandLineArguments
abstract class CustomCollectorArguments {

  @Parameter(repeatable = true, shortName = 'H', collectedBy = MyCollector.class)
  abstract Set<String> strings();

  static class MyCollector implements Supplier<Collector<String, ?, Set<String>>> {

    @Override
    public Collector<String, ?, Set<String>> get() {
      return Collectors.toSet();
    }
  }
}
