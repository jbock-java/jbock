package net.jbock.examples;

import net.jbock.CommandLineArguments;
import net.jbock.Positional;

@CommandLineArguments(addHelp = false)
abstract class HelplessArguments {

  @Positional
  abstract String required();

  abstract boolean help();
}
