package net.jbock.examples;

import net.jbock.CommandLineArguments;
import net.jbock.PositionalParameter;

import java.util.List;

@CommandLineArguments
abstract class GitArguments {

  @PositionalParameter(value = 1)
  abstract String command();

  @PositionalParameter(value = 2)
  abstract List<String> remainingArgs();
}
