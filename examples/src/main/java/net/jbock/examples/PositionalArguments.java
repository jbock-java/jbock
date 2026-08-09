package net.jbock.examples;

import net.jbock.Command;
import net.jbock.Parameter;

import java.util.List;
import java.util.Optional;

@Command
interface PositionalArguments {

    @Parameter(index = 0)
    String source();

    @Parameter(index = 1, description = "Desc of dest.")
    String dest();

    @Parameter(index = 2)
    int anotherInt();

    @Parameter(index = 3)
    Optional<String> optString();

    List<String> otherTokens();
}
