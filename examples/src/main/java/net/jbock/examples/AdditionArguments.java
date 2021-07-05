package net.jbock.examples;

import net.jbock.Command;
import net.jbock.Parameter;

import java.io.IOException;
import java.util.Optional;

@Command
abstract class AdditionArguments {

    AdditionArguments() throws IndexOutOfBoundsException {
    }

    AdditionArguments(String s) throws IOException {
    }

    /**
     * First argument
     */
    @Parameter(index = 0)
    abstract int a() throws IllegalStateException;

    /**
     * Second argument
     */
    @Parameter(index = 1)
    abstract int b();

    /**
     * Optional third argument
     */
    @Parameter(index = 2)
    abstract Optional<Integer> c();

    final int sum() {
        return a() + b() + c().orElse(0);
    }
}
