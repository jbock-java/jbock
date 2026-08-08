package net.jbock.examples;


import net.jbock.Command;
import net.jbock.Option;

@Command
interface TarArguments {

    @Option(names = {"--x", "-x"})
    boolean extract();

    @Option(names = {"--c", "-c"})
    boolean create();

    @Option(names = {"--v", "-v"})
    boolean verbose();

    @Option(names = {"--z", "-z"})
    boolean compress();

    @Option(names = {"--file", "-f"})
    String file();
}
