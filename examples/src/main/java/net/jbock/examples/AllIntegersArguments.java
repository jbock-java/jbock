package net.jbock.examples;

import net.jbock.CommandLineArguments;
import net.jbock.Parameter;
import net.jbock.PositionalParameter;

import java.util.List;
import java.util.Optional;

@CommandLineArguments
abstract class AllIntegersArguments {

  @PositionalParameter(value = 1)
  abstract List<Integer> positional();

  @Parameter(value = "i", mnemonic = 'i')
  abstract List<Integer> listOfIntegers();

  @Parameter(value = "opt")
  abstract Optional<Integer> optionalInteger();

  @Parameter(value = "obj")
  abstract Integer integer();

  @Parameter(value = "prim")
  abstract int primitiveInt();
}
