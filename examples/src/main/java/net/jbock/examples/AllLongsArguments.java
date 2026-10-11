package net.jbock.examples;

import net.jbock.Command;
import net.jbock.Option;
import net.jbock.Parameter;

import java.util.List;
import java.util.Optional;

@Command
interface AllLongsArguments {

    @Parameter
    List<Long> positional();

    @Option(names = {"--i", "-i"})
    List<Long> listOfLongs();

    @Option(names = "--opt")
    Optional<Long> optionalLong();

    @Option(names = "--obj")
    Long converters();

    @Option(names = "--prim")
    long primitiveLong();
}
