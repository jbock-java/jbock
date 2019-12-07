package net.jbock.examples;

import net.jbock.Command;
import net.jbock.Option;
import net.jbock.Param;

import java.util.List;
import java.util.Optional;

@Command
abstract class AllLongsArguments {

  @Param(1)
  abstract List<Long> positional();

  @Option(value = "i", mnemonic = 'i')
  abstract List<Long> listOfLongs();

  @Option("opt")
  abstract Optional<Long> optionalLong();

  @Option("obj")
  abstract Long longObject();

  @Option("prim")
  abstract long primitiveLong();
}
