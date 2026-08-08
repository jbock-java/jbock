package net.jbock.examples;

import net.jbock.Command;
import net.jbock.Option;
import net.jbock.VarargsParameter;

import java.util.List;
import java.util.Optional;

@Command
interface AllFloatsArguments {

    @VarargsParameter
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
