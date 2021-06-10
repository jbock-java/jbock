package net.jbock.examples;

import net.jbock.Command;
import net.jbock.Option;
import net.jbock.Parameter;

@Command(superCommand = true)
abstract class SuperArguments {

  @Option(names = {"--quiet", "-q"})
  abstract boolean quiet();

  @Parameter(index = 0)
  abstract String command();
}
