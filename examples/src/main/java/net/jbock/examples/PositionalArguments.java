package net.jbock.examples;

import io.jbock.util.Optional;
import net.jbock.Command;
import net.jbock.Parameter;
import net.jbock.Parameters;

import java.util.List;

@Command
abstract class PositionalArguments {

    @Parameter(index = 0)
    abstract String source();

    /**
     * Desc of dest.
     */
    @Parameter(index = 1)
    abstract String dest();

    @Parameter(index = 2)
    abstract int anotherInt();

    @Parameter(index = 3)
    abstract Optional<String> optString();

    @Parameters
    abstract List<String> otherTokens();
}
