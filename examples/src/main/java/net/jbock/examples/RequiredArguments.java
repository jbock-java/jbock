package net.jbock.examples;


import net.jbock.Command;
import net.jbock.Option;
import net.jbock.VarargsParameter;

import java.util.List;

@Command
abstract class RequiredArguments {

    @Option(names = "--dir")
    abstract String dir();

    @VarargsParameter
    abstract List<String> otherTokens();
}
