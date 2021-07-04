package net.jbock.examples;

import net.jbock.Command;
import net.jbock.Option;
import net.jbock.Parameter;

@Command
abstract class HelplessArguments {

    @Parameter(index = 0)
    abstract String required();

    @Option(names = "--help")
    abstract boolean help();
}
