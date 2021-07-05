package net.jbock.examples;

import net.jbock.Command;
import net.jbock.Option;
import net.jbock.Parameter;

import java.util.Optional;

@Command(unixClustering = false)
abstract class CpArguments {

    enum Control {
        NONE,
        NUMBERED,
        EXISTING,
        SIMPLE
    }

    @Parameter(index = 0)
    abstract String source();

    @Parameter(index = 1)
    abstract String dest();

    @Option(names = {"--r", "-r"})
    abstract boolean recursive();

    @Option(names = "--backup")
    abstract Optional<Control> backup();


    /**
     * Override the usual backup suffix
     */
    @Option(names = {"--suffix", "-s"})
    abstract Optional<String> suffix();
}
