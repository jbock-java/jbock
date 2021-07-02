package net.jbock.examples;

import net.jbock.Command;
import net.jbock.Option;
import net.jbock.Parameters;
import net.jbock.either.Optional;

import java.util.List;

@Command
abstract class AllDoublesArguments {

  @Parameters
  abstract List<Double> positional();

  @Option(names = {"--i", "-i"})
  abstract List<Double> listOfDoubles();

  @Option(names = "--opt")
  abstract Optional<Double> optionalDouble();

  @Option(names = "--obj")
  abstract Double doubleObject();

  @Option(names = "--prim")
  abstract double primitiveDouble();
}
