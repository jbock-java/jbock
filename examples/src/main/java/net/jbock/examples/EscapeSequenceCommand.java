package net.jbock.examples;

import net.jbock.Command;
import net.jbock.Option;
import net.jbock.Parameter;
import net.jbock.VarargsParameter;

import java.util.List;

@Command
interface EscapeSequenceCommand {

    @Parameter(index = 0)
    String command();

    @Option(names = "--bare")
    boolean bare();

    @VarargsParameter
    List<String> remainingArgs();
}
