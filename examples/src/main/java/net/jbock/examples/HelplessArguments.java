package net.jbock.examples;

import net.jbock.Command;
import net.jbock.Option;
import net.jbock.Param;

@Command(helpEnabled = false)
abstract class HelplessArguments {

  @Param(0)
  abstract String required();

  @Option(names = "--help")
  abstract boolean help();
}
