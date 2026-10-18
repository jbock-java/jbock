package net.jbock.examples;

import net.jbock.ColorMode;
import net.jbock.Command;
import net.jbock.Option;
import net.jbock.Parameter;

import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

@Command(colorMode = ColorMode.NO_COLOR)
interface AllIntegersArguments {

    @Parameter
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
