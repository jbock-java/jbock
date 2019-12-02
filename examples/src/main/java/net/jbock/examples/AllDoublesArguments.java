package net.jbock.examples;

import net.jbock.CLI;
import net.jbock.Option;
import net.jbock.Param;

import java.util.List;
import java.util.Optional;

@CLI
abstract class AllDoublesArguments {

  @Param(value = 1)
  abstract List<Double> positional();

  @Option(value = "i", mnemonic = 'i')
  abstract List<Double> listOfDoubles();

  @Option(value = "opt")
  abstract Optional<Double> optionalDouble();

  @Option(value = "obj")
  abstract Double doubleObject();

  @Option(value = "prim")
  abstract double primitiveDouble();
}
