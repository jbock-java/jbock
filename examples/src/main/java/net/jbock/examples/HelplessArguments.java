package net.jbock.examples;

import net.jbock.Command;
import net.jbock.Option;
import net.jbock.Param;

@Command(helpDisabled = true)
abstract class HelplessArguments {

  @Param(0)
  abstract String required();

  @Option("help")
  abstract boolean help();
}
