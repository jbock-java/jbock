package net.jbock.examples;

import net.jbock.CommandLineArguments;
import net.jbock.Positional;

@CommandLineArguments(helpDisabled = true)
abstract class HelplessArguments {

  @Positional
  abstract String required();

  abstract boolean help();
}
