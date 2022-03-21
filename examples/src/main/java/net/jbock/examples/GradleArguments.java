package net.jbock.examples;


import net.jbock.Command;
import net.jbock.Option;
import net.jbock.Parameter;
import net.jbock.VarargsParameter;

import java.util.List;
import java.util.Optional;

@Command
abstract class GradleArguments {

    @Option(names = {"--message", "-m"},
            description = {"the message", "message goes here"})
    abstract Optional<String> message();

    @Option(names = {"--file", "-f"},
            paramLabel = "INPUT_FILE",
            description = "the files")
    abstract List<String> file();

    @Option(names = "--dir",
            paramLabel = "INPUT_DIR",
            description = "the dir")
    abstract Optional<String> dir();

    @Option(names = {"--c", "-c"},
            paramLabel = "THIS_IS_IGNORED",
            description = "cmos flag")
    abstract boolean cmos();

    @Option(names = {"--verbose", "-v"})
    abstract boolean verbose();

    @Parameter(index = 0,
            paramLabel = "SOME_TOKEN",
            description = "some token")
    abstract Optional<String> mainToken();

    @VarargsParameter(paramLabel = "moreTokens",
            description = "some more tokens")
    abstract List<String> otherTokens();

    @Command
    static abstract class Foo {

        @Option(names = "--bar")
        abstract Optional<Integer> bar();
    }

    @Command
    static abstract class Bar {

        @Option(names = "--bar")
        abstract List<String> bar();
    }
}
