package net.jbock.examples;

import net.jbock.Command;
import net.jbock.Option;
import net.jbock.Parameter;

import java.util.Optional;

@Command
interface CpArguments {

    enum Control {
        NONE,
        NUMBERED,
        EXISTING,
        SIMPLE
    }

    @Parameter(index = 0)
    String source();

    @Parameter(index = 1)
    String dest();

    @Option(names = {"--r", "-r"})
    boolean recursive();

    @Option(names = "--backup")
    Optional<Control> backup();

    @Option(names = {"--suffix", "-s"},
            description = "Override the usual backup suffix")
    Optional<String> suffix();
}
