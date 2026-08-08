package net.jbock.examples;

import net.jbock.Command;
import net.jbock.Parameter;

@Command
interface MvArguments {

    @Parameter(index = 0)
    String source();

    @Parameter(index = 1)
    String dest();

    default boolean isSafe() {
        return true;
    }
}
