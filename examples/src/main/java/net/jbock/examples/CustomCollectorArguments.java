package net.jbock.examples;

import net.jbock.CommandLineArguments;
import net.jbock.Parameter;

import java.math.BigInteger;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@CommandLineArguments
abstract class CustomCollectorArguments {

  @Parameter(repeatable = true, shortName = 'H', collectedBy = MyStringCollector.class)
  abstract Set<String> strings();

  @Parameter(repeatable = true, shortName = 'B', collectedBy = MyIntegerCollector.class)
  abstract Set<Integer> integers();

  @Parameter(
      repeatable = true,
      shortName = 'M',
      mappedBy = CustomBigIntegerMapper.class,
      collectedBy = ToSetCollector.class)
  abstract Set<BigInteger> bigIntegers();

  static class MyStringCollector implements Supplier<Collector<String, ?, Set<String>>> {

    @Override
    public Collector<String, ?, Set<String>> get() {
      return Collectors.toSet();
    }
  }

  static class MyBigIntegerCollector implements Supplier<Collector<BigInteger, ?, Set<BigInteger>>> {

    @Override
    public Collector<BigInteger, ?, Set<BigInteger>> get() {
      return Collectors.toSet();
    }
  }

  static class MyIntegerCollector implements Supplier<Collector<Integer, ?, Set<Integer>>> {

    @Override
    public Collector<Integer, ?, Set<Integer>> get() {
      return Collectors.toSet();
    }
  }

  static class ToSetCollector<E> implements Supplier<Collector<E, ?, Set<E>>> {

    @Override
    public Collector<E, ?, Set<E>> get() {
      return Collectors.toSet();
    }
  }
}
