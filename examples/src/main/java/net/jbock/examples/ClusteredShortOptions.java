package net.jbock.examples;

import net.jbock.Command;
import net.jbock.Option;

@Command
interface ClusteredShortOptions {

    @Option(names = {"-a", "--aa"})
    boolean aaa();

    @Option(names = "-b")
    boolean bbb();

    @Option(names = "-c")
    boolean ccc();

    @Option(names = "-f")
    String file();
}
