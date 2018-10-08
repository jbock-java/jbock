package net.jbock.examples;

import net.jbock.CommandLineArguments;
import net.jbock.Parameter;
import net.jbock.PositionalParameter;

import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

@CommandLineArguments
abstract class AllIntegersArguments {

  @PositionalParameter(repeatable = true)
  abstract List<Integer> positional();

  @Parameter(repeatable = true, shortName = 'i')
  abstract List<Integer> listOfIntegers();

  @Parameter(optional = true, longName = "opt")
  abstract Optional<Integer> optionalInteger();

  @Parameter(optional = true, longName = "optint")
  abstract OptionalInt optionalInt();

  @Parameter(longName = "obj")
  abstract Integer integer();

  @Parameter(longName = "prim")
  abstract int primitiveInt();
}
