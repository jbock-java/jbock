package net.jbock.examples;

import io.jbock.util.Optional;
import net.jbock.Command;
import net.jbock.Option;
import net.jbock.Parameters;

import java.util.List;
import java.util.OptionalInt;

@Command
abstract class AllIntegersArguments {

    @Parameters
    abstract List<Integer> positional();

    @Option(names = {"--i", "-i"})
    abstract List<Integer> listOfIntegers();

    @Option(names = "--opt")
    abstract Optional<Integer> optionalInteger();

    @Option(names = "--obj")
    abstract Integer integer();

    @Option(names = "--prim")
    abstract int primitiveInt();

    @Option(names = "--opti")
    abstract OptionalInt optionalInt();
}
