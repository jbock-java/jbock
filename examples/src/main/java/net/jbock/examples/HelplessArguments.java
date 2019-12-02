package net.jbock.examples;

import net.jbock.CLI;
import net.jbock.Option;
import net.jbock.Param;

@CLI(helpDisabled = true)
abstract class HelplessArguments {

  @Param(1)
  abstract String required();

  @Option("help")
  abstract boolean help();
}
