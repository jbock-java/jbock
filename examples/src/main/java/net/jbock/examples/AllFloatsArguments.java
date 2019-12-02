package net.jbock.examples;

import net.jbock.CLI;
import net.jbock.Option;
import net.jbock.Param;

import java.util.List;
import java.util.Optional;

@CLI
abstract class AllFloatsArguments {

  @Param(value = 1)
  abstract List<Float> positional();

  @Option(value = "i", mnemonic = 'i')
  abstract List<Float> listOfFloats();

  @Option(value = "opt")
  abstract Optional<Float> optionalFloat();

  @Option(value = "obj")
  abstract Float floatObject();

  @Option(value = "prim")
  abstract float primitiveFloat();
}
