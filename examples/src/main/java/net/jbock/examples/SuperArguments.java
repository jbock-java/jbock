package net.jbock.examples;

import net.jbock.Option;
import net.jbock.Parameter;
import net.jbock.Command;

import java.util.List;

@Command(superCommand = true)
interface SuperArguments {

    @Option(names = {"--quiet", "-q"})
    boolean quiet();

    @Parameter(index = 0)
    String command();

    List<String> rest();
}
