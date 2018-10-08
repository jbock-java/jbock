package net.jbock.examples;

import net.jbock.CommandLineArguments;
import net.jbock.Parameter;
import net.jbock.PositionalParameter;

@CommandLineArguments(addHelp = false)
abstract class HelplessArguments {

  @PositionalParameter
  abstract String required();

  @Parameter(flag = true)
  abstract boolean help();
}
