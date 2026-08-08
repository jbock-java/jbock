package net.jbock.examples;

import net.jbock.Command;
import net.jbock.Option;
import net.jbock.Parameter;

@Command
interface HelplessArguments {

    @Parameter(index = 0)
    String required();

    @Option(names = "--help")
    boolean help();
}
