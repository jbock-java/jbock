package net.jbock.examples;


import net.jbock.Command;
import net.jbock.Option;

@Command
abstract class TarArguments {

  @Option(names = {"--x", "-x"})
  abstract boolean extract();

  @Option(names = {"--c", "-c"})
  abstract boolean create();

  @Option(names = {"--v", "-v"})
  abstract boolean verbose();

  @Option(names = {"--z", "-z"})
  abstract boolean compress();

  @Option(names = {"--f", "-f"})
  abstract String file();
}
