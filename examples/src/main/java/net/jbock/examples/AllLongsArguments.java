package net.jbock.examples;

import net.jbock.Command;
import net.jbock.Option;
import net.jbock.Parameters;

import java.util.List;
import java.util.Optional;

@Command
abstract class AllLongsArguments {

  @Parameters
  abstract List<Long> positional();

  @Option(names = {"--i", "-i"})
  abstract List<Long> listOfLongs();

  @Option(names = "--opt")
  abstract Optional<Long> optionalLong();

  @Option(names = "--obj")
  abstract Long longObject();

  @Option(names = "--prim")
  abstract long primitiveLong();
}
