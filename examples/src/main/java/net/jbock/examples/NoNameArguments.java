package net.jbock.examples;


import net.jbock.Command;
import net.jbock.Option;

import java.util.List;
import java.util.Optional;

@Command
interface NoNameArguments {

    @Option(names = "--message")
    Optional<String> message();

    @Option(names = "--file")
    List<String> file();

    @Option(names = {"--verbosity", "-v"})
    Optional<Integer> verbosity();

    @Option(names = {"--number", "-n"})
    int number();

    @Option(names = "--cmos")
    boolean cmos();
}
