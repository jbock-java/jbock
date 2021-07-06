package net.jbock.examples;

import net.jbock.Command;
import net.jbock.Option;

import java.util.Optional;

@Command
abstract class PsArguments {

    @Option(names = {"--all", "-a"})
    abstract boolean all();

    @Option(names = {"--width", "-w"},
            description = "This is the description.")
    abstract Optional<Integer> width();
}
