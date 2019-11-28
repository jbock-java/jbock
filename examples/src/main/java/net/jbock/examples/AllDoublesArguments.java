package net.jbock.examples;

import net.jbock.CommandLineArguments;
import net.jbock.Parameter;
import net.jbock.PositionalParameter;

import java.util.List;
import java.util.Optional;

@CommandLineArguments
abstract class AllDoublesArguments {

  @PositionalParameter(value = 1)
  abstract List<Double> positional();

  @Parameter(value = "i", mnemonic = 'i')
  abstract List<Double> listOfDoubles();

  @Parameter(value = "opt")
  abstract Optional<Double> optionalDouble();

  @Parameter(value = "obj")
  abstract Double doubleObject();

  @Parameter(value = "prim")
  abstract double primitiveDouble();
}
