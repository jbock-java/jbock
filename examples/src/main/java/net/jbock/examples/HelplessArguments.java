package net.jbock.examples;

import net.jbock.CommandLineArguments;
import net.jbock.Parameter;
import net.jbock.PositionalParameter;

@CommandLineArguments(helpDisabled = true)
abstract class HelplessArguments {

  @PositionalParameter(1)
  abstract String required();

  @Parameter("help")
  abstract boolean help();
}
