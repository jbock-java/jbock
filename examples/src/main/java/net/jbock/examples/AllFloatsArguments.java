package net.jbock.examples;

import net.jbock.Command;
import net.jbock.Option;

import java.util.List;
import java.util.Optional;

@Command
interface AllFloatsArguments {

    List<Float> positional();

    @Option(names = {"--i", "-i"})
    List<Float> listOfFloats();

    @Option(names = "--opt")
    Optional<Float> optionalFloat();

    @Option(names = "--obj")
    Float floatObject();

    @Option(names = "--prim")
    float primitiveFloat();
}
