package net.jbock.examples;

import net.jbock.Command;
import net.jbock.Option;

@Command
abstract class AllFlagsArguments {

    @Option(names = {"--smallFlag", "-f", "-s"})
    abstract boolean smallFlag();
}
