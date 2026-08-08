package net.jbock.examples;

import net.jbock.Command;
import net.jbock.Option;

import java.util.Optional;

@Command
interface PsArguments {

    @Option(names = {"--all", "-a"})
    boolean all();

    @Option(names = {"--width", "-w"},
            description = "This is the description.")
    Optional<Integer> width();
}
