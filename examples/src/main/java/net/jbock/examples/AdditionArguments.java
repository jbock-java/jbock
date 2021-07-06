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

    @Parameter(index = 0, description = "First argument")
    abstract int a() throws IllegalStateException;

    @Parameter(index = 1, description = "Second argument")
    abstract int b();

    @Parameter(index = 2, description = {"Optional", "third", "argument"})
    abstract Optional<Integer> c();

    final int sum() {
        return a() + b() + c().orElse(0);
    }
}
