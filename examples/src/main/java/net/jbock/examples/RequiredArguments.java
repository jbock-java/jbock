package net.jbock.examples;


import net.jbock.Command;
import net.jbock.Option;

import java.util.List;

@Command
interface RequiredArguments {

    @Option(names = "--dir")
    String dir();

    List<String> otherTokens();
}
