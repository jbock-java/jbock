package net.jbock.examples;

import net.jbock.Command;
import net.jbock.Option;

@Command(unixClustering = true)
abstract class ClusteredShortOptions {

  @Option(names = {"-a", "--aa"})
  abstract boolean aaa();

  @Option(names = "-b")
  abstract boolean bbb();

  @Option(names = "-c")
  abstract boolean ccc();

  @Option(names = "-f")
  abstract String file();
}
