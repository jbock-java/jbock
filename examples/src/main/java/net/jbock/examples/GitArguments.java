package net.jbock.examples;

import net.jbock.CommandLineArguments;
import net.jbock.PositionalParameter;

@CommandLineArguments(strict = false, allowEscape = false)
abstract class GitArguments {

  @PositionalParameter
  abstract String command();

  @PositionalParameter
  abstract String[] remainingArgs();
}
