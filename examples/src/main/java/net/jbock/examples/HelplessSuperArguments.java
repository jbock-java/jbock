package net.jbock.examples;

import net.jbock.Option;
import net.jbock.Parameter;
import net.jbock.SuperCommand;
import net.jbock.VarargsParameter;

import java.util.List;

@SuperCommand
abstract class HelplessSuperArguments {

    @Option(names = {"--quiet", "-q"})
    abstract boolean quiet();

    @Parameter(index = 0)
    abstract String command();

    @VarargsParameter
    abstract List<String> rest();
}
