package net.jbock.examples;

import net.jbock.Command;
import net.jbock.Option;

import java.util.List;
import java.util.Optional;

@Command
interface AllDoublesArguments {

    List<Double> positional();

    @Option(names = {"--i", "-i"})
    List<Double> listOfDoubles();

    @Option(names = "--opt")
    Optional<Double> optionalDouble();

    @Option(names = "--obj")
    Double doubleObject();

    @Option(names = "--prim")
    double primitiveDouble();
}
