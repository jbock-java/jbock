package net.jbock.examples;

import net.jbock.Command;
import net.jbock.Parameter;

import java.io.IOException;
import java.util.Optional;

@Command
interface AdditionArguments {

    @Parameter(index = 0, description = "First argument")
    int a() throws IllegalStateException;

    @Parameter(index = 1, description = "Second argument")
    int b();

    @Parameter(index = 2, description = {"Optional", "third", "argument"})
    Optional<Integer> c();

    default int sum() {
        return a() + b() + c().orElse(0);
    }
}
