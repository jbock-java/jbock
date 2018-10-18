package net.jbock.examples;

import net.jbock.CommandLineArguments;
import net.jbock.Parameter;
import net.jbock.PositionalParameter;

@CommandLineArguments(allowHelpOption = false)
abstract class HelplessArguments {

  @PositionalParameter
  abstract String required();

  @Parameter(longName = "help", flag = true)
  abstract boolean help();
}
