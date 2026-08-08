package net.jbock.examples;


import net.jbock.Command;
import net.jbock.Option;
import net.jbock.VarargsParameter;

import java.util.List;

@Command
interface RequiredArguments {

    @Option(names = "--dir")
    String dir();

    @VarargsParameter
    List<String> otherTokens();
}
