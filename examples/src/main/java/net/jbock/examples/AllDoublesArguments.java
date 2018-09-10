package net.jbock.examples;

import net.jbock.CommandLineArguments;
import net.jbock.LongName;
import net.jbock.Positional;
import net.jbock.ShortName;

import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;

@CommandLineArguments
abstract class AllDoublesArguments {

  @Positional
  abstract List<Double> positional();

  @ShortName('i')
  abstract List<Double> listOfDoubles();

  @LongName("opt")
  abstract Optional<Double> optionalDouble();

  @LongName("optdouble")
  abstract OptionalDouble optionalPrimitiveDouble();

  @LongName("obj")
  abstract Double doubleObject();

  @LongName("prim")
  abstract double primitiveDouble();
}
