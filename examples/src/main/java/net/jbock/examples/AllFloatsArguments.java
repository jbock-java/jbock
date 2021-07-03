package net.jbock.examples;

import io.jbock.util.Optional;
import net.jbock.Command;
import net.jbock.Option;
import net.jbock.Parameters;

import java.util.List;

@Command
abstract class AllFloatsArguments {

    @Parameters
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
