package net.jbock.examples;

import net.jbock.Command;
import net.jbock.Option;
import net.jbock.Parameter;

@Command(helpEnabled = false, superCommand = true)
abstract class HelplessSuperArguments {

  @Option(names = {"--quiet", "-q"})
  abstract boolean quiet();

  @Parameter(index = 0)
  abstract String command();
}
