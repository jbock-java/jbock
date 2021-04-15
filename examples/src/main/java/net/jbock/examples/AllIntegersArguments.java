package net.jbock.examples;

import net.jbock.Command;
import net.jbock.Option;
import net.jbock.Param;

import java.util.List;
import java.util.Optional;

@Command
abstract class AllIntegersArguments {

  @Param(0)
  abstract List<Integer> positional();

  @Option(value = "i", mnemonic = 'i')
  abstract List<Integer> listOfIntegers();

  @Option("opt")
  abstract Optional<Integer> optionalInteger();

  @Option("obj")
  abstract Integer integer();

  @Option("prim")
  abstract int primitiveInt();
}
