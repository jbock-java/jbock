package net.jbock.examples;

import net.jbock.Option;
import net.jbock.Parameter;
import net.jbock.SuperCommand;

import java.util.List;

@SuperCommand
interface SuperArguments {

    @Option(names = {"--quiet", "-q"})
    boolean quiet();

    @Parameter(index = 0)
    String command();

    List<String> rest();
}
