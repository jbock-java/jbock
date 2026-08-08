package net.jbock.examples;

import net.jbock.Command;
import net.jbock.Option;
import net.jbock.VarargsParameter;

import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

@Command
interface AllIntegersArguments {

    @VarargsParameter
    List<Integer> positional();

    @Option(names = {"--i", "-i"})
    List<Integer> listOfIntegers();

    @Option(names = "--opt")
    Optional<Integer> optionalInteger();

    @Option(names = "--obj")
    Integer integer();

    @Option(names = "--prim")
    int primitiveInt();

    @Option(names = "--opti")
    OptionalInt optionalInt();
}
