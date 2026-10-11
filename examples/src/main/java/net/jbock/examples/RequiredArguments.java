package net.jbock.examples;


import net.jbock.Command;
import net.jbock.Option;
import net.jbock.Parameter;

import java.util.List;

@Command
interface RequiredArguments {

    @Option(names = "--dir")
    String dir();

    @Parameter
    List<String> otherTokens();
}
