package net.jbock.examples;

import net.jbock.CommandLineArguments;
import net.jbock.Parameter;
import net.jbock.PositionalParameter;

import java.util.List;
import java.util.Optional;

@CommandLineArguments
abstract class AllFloatsArguments {

  @PositionalParameter
  abstract List<Float> positional();

  @Parameter(shortName = 'i')
  abstract List<Float> listOfFloats();

  @Parameter(longName = "opt")
  abstract Optional<Float> optionalFloat();

  @Parameter(longName = "obj")
  abstract Float floatObject();

  @Parameter(longName = "prim")
  abstract float primitiveFloat();
}
