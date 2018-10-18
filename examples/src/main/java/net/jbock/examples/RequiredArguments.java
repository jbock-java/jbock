package net.jbock.examples;


import net.jbock.CommandLineArguments;
import net.jbock.Parameter;
import net.jbock.PositionalParameter;

import java.util.List;

@CommandLineArguments(allowEscapeSequence = true)
abstract class RequiredArguments {

  @Parameter(longName = "dir")
  abstract String dir();

  @PositionalParameter(repeatable = true)
  abstract List<String> otherTokens();
}
