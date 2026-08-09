package net.jbock.examples;

import net.jbock.Command;
import net.jbock.Option;
import net.jbock.Parameter;

import java.util.List;

@Command
interface EscapeSequenceCommand {

    @Parameter(index = 0)
    String command();

    @Option(names = "--bare")
    boolean bare();

    List<String> remainingArgs();
}
