package net.jbock.examples;

import net.jbock.Command;
import net.jbock.Option;

@Command
interface AllFlagsArguments {

    @Option(names = {"--smallFlag", "-f", "-s"})
    boolean smallFlag();
}
