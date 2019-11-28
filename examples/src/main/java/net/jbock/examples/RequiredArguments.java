package net.jbock.examples;


import net.jbock.CommandLineArguments;
import net.jbock.Parameter;
import net.jbock.PositionalParameter;

import java.util.List;

@CommandLineArguments
abstract class RequiredArguments {

  @Parameter("dir")
  abstract String dir();

  @PositionalParameter(1)
  abstract List<String> otherTokens();
}
