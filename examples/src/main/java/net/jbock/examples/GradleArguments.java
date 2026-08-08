package net.jbock.examples;


import net.jbock.Command;
import net.jbock.Option;
import net.jbock.Parameter;
import net.jbock.VarargsParameter;

import java.util.List;
import java.util.Optional;

@Command
interface GradleArguments {

    @Option(names = {"--message", "-m"},
            description = {"the message", "message goes here"})
    Optional<String> message();

    @Option(names = {"--file", "-f"},
            paramLabel = "INPUT_FILE",
            description = "the files")
    List<String> file();

    @Option(names = "--dir",
            paramLabel = "INPUT_DIR",
            description = "the dir")
    Optional<String> dir();

    @Option(names = {"--c", "-c"},
            paramLabel = "THIS_IS_IGNORED",
            description = "cmos flag")
    boolean cmos();

    @Option(names = {"--verbose", "-v"})
    boolean verbose();

    @Parameter(index = 0,
            paramLabel = "SOME_TOKEN",
            description = "some token")
    Optional<String> mainToken();

    @VarargsParameter(paramLabel = "moreTokens",
            description = "some more tokens")
    List<String> otherTokens();

    @Command
    interface Foo {

        @Option(names = "--bar")
        Optional<Integer> bar();
    }

    @Command
    interface Bar {

        @Option(names = "--bar")
        List<String> bar();
    }
}
