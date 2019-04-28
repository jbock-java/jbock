package net.jbock.examples;

import net.jbock.CommandLineArguments;
import net.jbock.Parameter;
import net.jbock.PositionalParameter;

import java.util.List;
import java.util.Optional;

@CommandLineArguments
abstract class AllIntegersArguments {

  @PositionalParameter
  abstract List<Integer> positional();

  @Parameter(shortName = 'i')
  abstract List<Integer> listOfIntegers();

  @Parameter(longName = "opt")
  abstract Optional<Integer> optionalInteger();

  @Parameter(longName = "obj")
  abstract Integer integer();

  @Parameter(longName = "prim")
  abstract int primitiveInt();
}
