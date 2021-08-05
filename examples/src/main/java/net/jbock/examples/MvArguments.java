package net.jbock.examples;

import net.jbock.Command;
import net.jbock.Parameter;

@Command
abstract class MvArguments {

    @Parameter(index = 0)
    abstract String source();

    @Parameter(index = 1)
    abstract String dest();

    public boolean isSafe() {
        return true;
    }
}
