package net.jbock.examples;

import net.jbock.Option;
import net.jbock.Param;
import net.jbock.SuperCommand;

@SuperCommand(helpEnabled = false)
abstract class HelplessSuperArguments {

  @Option(names = {"--quiet", "-q"})
  abstract boolean quiet();

  @Param(0)
  abstract String command();
}
