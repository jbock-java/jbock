package net.jbock.examples;

import net.jbock.CommandLineArguments;
import net.jbock.PositionalParameter;

import java.util.List;

@CommandLineArguments(strict = false, allowEscape = false)
abstract class GitArguments {

  @PositionalParameter
  abstract String command();

  @PositionalParameter(repeatable = true)
  abstract List<String> remainingArgs();
}
