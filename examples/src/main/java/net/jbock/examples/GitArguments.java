package net.jbock.examples;

import net.jbock.CommandLineArguments;
import net.jbock.PositionalParameter;

import java.util.List;

@CommandLineArguments(allowPrefixedTokens = true)
abstract class GitArguments {

  @PositionalParameter
  abstract String command();

  @PositionalParameter(repeatable = true)
  abstract List<String> remainingArgs();
}
