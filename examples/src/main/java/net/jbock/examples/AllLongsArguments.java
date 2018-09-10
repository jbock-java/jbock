package net.jbock.examples;

import net.jbock.CommandLineArguments;
import net.jbock.LongName;
import net.jbock.Positional;
import net.jbock.ShortName;

import java.util.List;
import java.util.Optional;
import java.util.OptionalLong;

@CommandLineArguments
abstract class AllLongsArguments {

  @Positional
  abstract List<Long> positional();

  @ShortName('i')
  abstract List<Long> listOfLongs();

  @LongName("opt")
  abstract Optional<Long> optionalLong();

  @LongName("optlong")
  abstract OptionalLong optionalPrimitiveLong();

  @LongName("obj")
  abstract Long longObject();

  @LongName("prim")
  abstract long primitiveLong();
}
