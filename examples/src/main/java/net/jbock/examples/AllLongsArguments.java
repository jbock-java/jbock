package net.jbock.examples;

import net.jbock.Command;
import net.jbock.Option;
import net.jbock.VarargsParameter;

import java.util.List;
import java.util.Optional;

@Command
interface AllLongsArguments {

    @VarargsParameter
    List<Long> positional();

    @Option(names = {"--i", "-i"})
    List<Long> listOfLongs();

    @Option(names = "--opt")
    Optional<Long> optionalLong();

    @Option(names = "--obj")
    Long longObject();

    @Option(names = "--prim")
    long primitiveLong();
}
