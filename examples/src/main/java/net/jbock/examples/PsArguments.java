package net.jbock.examples;

import io.jbock.util.Optional;
import net.jbock.Command;
import net.jbock.Option;

@Command
abstract class PsArguments {

    @Option(names = {"--all", "-a"})
    abstract boolean all();

    /**
     * This is the description.
     */
    @Option(names = {"--width", "-w"})
    abstract Optional<Integer> width();
}
