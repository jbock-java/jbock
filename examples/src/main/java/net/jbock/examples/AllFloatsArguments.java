package net.jbock.examples;

import net.jbock.CommandLineArguments;
import net.jbock.LongName;
import net.jbock.Positional;
import net.jbock.ShortName;

import java.util.List;
import java.util.Optional;

@CommandLineArguments
abstract class AllFloatsArguments {

  @Positional
  abstract List<Float> positional();

  @ShortName('i')
  abstract List<Float> listOfFloats();

  @LongName("opt")
  abstract Optional<Float> optionalFloat();

  @LongName("obj")
  abstract Float floatObject();

  @LongName("prim")
  abstract float primitiveFloat();
}
