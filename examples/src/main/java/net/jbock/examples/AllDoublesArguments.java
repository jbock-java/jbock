package net.jbock.examples;

import net.jbock.CommandLineArguments;
import net.jbock.Parameter;
import net.jbock.PositionalParameter;

import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;

@CommandLineArguments
abstract class AllDoublesArguments {

  @PositionalParameter
  abstract List<Double> positional();

  @Parameter(shortName = 'i')
  abstract List<Double> listOfDoubles();

  @Parameter(longName = "opt")
  abstract Optional<Double> optionalDouble();

  @Parameter(longName = "optdouble")
  abstract OptionalDouble optionalPrimitiveDouble();

  @Parameter(longName = "obj")
  abstract Double doubleObject();

  @Parameter(longName = "prim")
  abstract double primitiveDouble();
}
