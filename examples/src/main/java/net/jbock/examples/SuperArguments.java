package net.jbock.examples;

import net.jbock.Option;
import net.jbock.Parameter;
import net.jbock.SuperCommand;

@SuperCommand
abstract class SuperArguments {

    @Option(names = {"--quiet", "-q"})
    abstract boolean quiet();

    @Parameter(index = 0)
    abstract String command();
}
