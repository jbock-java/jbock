package net.jbock.examples;

import net.jbock.Command;
import net.jbock.Option;
import net.jbock.VarargsParameter;

import java.util.List;
import java.util.Optional;

@Command
abstract class AllFloatsArguments {

    @VarargsParameter
    abstract List<Float> positional();

    @Option(names = {"--i", "-i"})
    abstract List<Float> listOfFloats();

    @Option(names = "--opt")
    abstract Optional<Float> optionalFloat();

    @Option(names = "--obj")
    abstract Float floatObject();

    @Option(names = "--prim")
    abstract float primitiveFloat();
}
