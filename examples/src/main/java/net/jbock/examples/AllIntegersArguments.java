package net.jbock.examples;

import net.jbock.CLI;
import net.jbock.Option;
import net.jbock.Param;

import java.util.List;
import java.util.Optional;

@CLI
abstract class AllIntegersArguments {

  @Param(value = 1)
  abstract List<Integer> positional();

  @Option(value = "i", mnemonic = 'i')
  abstract List<Integer> listOfIntegers();

  @Option(value = "opt")
  abstract Optional<Integer> optionalInteger();

  @Option(value = "obj")
  abstract Integer integer();

  @Option(value = "prim")
  abstract int primitiveInt();
}
