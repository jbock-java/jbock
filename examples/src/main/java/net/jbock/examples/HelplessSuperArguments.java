package net.jbock.examples;

import net.jbock.Option;
import net.jbock.Parameter;
import net.jbock.SuperCommand;
import net.jbock.VarargsParameter;

import java.util.List;

@SuperCommand
interface HelplessSuperArguments {

    @Option(names = {"--quiet", "-q"})
    boolean quiet();

    @Parameter(index = 0)
    String command();

    @VarargsParameter
    List<String> rest();
}
