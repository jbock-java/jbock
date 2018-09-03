package net.jbock.examples;

import net.jbock.CommandLineArguments;
import net.jbock.Positional;

@CommandLineArguments(strict = false, allowEscape = false)
abstract class GitArguments {

  @Positional
  abstract String command();

  @Positional
  abstract String[] remainingArgs();
}
