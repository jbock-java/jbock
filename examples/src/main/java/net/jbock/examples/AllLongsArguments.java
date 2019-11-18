package net.jbock.examples;

import net.jbock.CommandLineArguments;
import net.jbock.Parameter;
import net.jbock.PositionalParameter;

import java.util.List;
import java.util.Optional;

@CommandLineArguments
abstract class AllLongsArguments {

  @PositionalParameter(value = 1)
  abstract List<Long> positional();

  @Parameter(value = "i", mnemonic = 'i')
  abstract List<Long> listOfLongs();

  @Parameter(value = "opt")
  abstract Optional<Long> optionalLong();

  @Parameter(value = "obj")
  abstract Long longObject();

  @Parameter(value = "prim")
  abstract long primitiveLong();
}
