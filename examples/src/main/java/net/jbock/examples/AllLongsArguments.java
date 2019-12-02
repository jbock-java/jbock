package net.jbock.examples;

import net.jbock.CLI;
import net.jbock.Option;
import net.jbock.Param;

import java.util.List;
import java.util.Optional;

@CLI
abstract class AllLongsArguments {

  @Param(value = 1)
  abstract List<Long> positional();

  @Option(value = "i", mnemonic = 'i')
  abstract List<Long> listOfLongs();

  @Option(value = "opt")
  abstract Optional<Long> optionalLong();

  @Option(value = "obj")
  abstract Long longObject();

  @Option(value = "prim")
  abstract long primitiveLong();
}
