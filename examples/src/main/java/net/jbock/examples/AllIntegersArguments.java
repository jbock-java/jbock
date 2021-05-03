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

  @Option(names = {"--i", "-i"})
  abstract List<Integer> listOfIntegers();

  @Option(names = "--opt")
  abstract Optional<Integer> optionalInteger();

  @Option(names = "--obj")
  abstract Integer integer();

  @Option(names = "--prim")
  abstract int primitiveInt();
}
