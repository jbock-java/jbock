package net.jbock.examples;

import net.jbock.Command;
import net.jbock.Parameter;
import net.jbock.VarargsParameter;

import java.util.List;
import java.util.Optional;

@Command
abstract class PositionalArguments {

    @Parameter(index = 0)
    abstract String source();

    @Parameter(index = 1, description = "Desc of dest.")
    abstract String dest();

    @Parameter(index = 2)
    abstract int anotherInt();

    @Parameter(index = 3)
    abstract Optional<String> optString();

    @VarargsParameter
    abstract List<String> otherTokens();
}
