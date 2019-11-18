package net.jbock.examples;

import net.jbock.CommandLineArguments;
import net.jbock.Parameter;
import net.jbock.PositionalParameter;

import java.util.List;
import java.util.Optional;

@CommandLineArguments
abstract class AllFloatsArguments {

  @PositionalParameter(value = 1)
  abstract List<Float> positional();

  @Parameter(value = "i", mnemonic = 'i')
  abstract List<Float> listOfFloats();

  @Parameter(value = "opt")
  abstract Optional<Float> optionalFloat();

  @Parameter(value = "obj")
  abstract Float floatObject();

  @Parameter(value = "prim")
  abstract float primitiveFloat();
}
