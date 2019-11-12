package net.jbock.examples;

import net.jbock.CommandLineArguments;
import net.jbock.Parameter;
import net.jbock.PositionalParameter;

import java.util.List;

@CommandLineArguments
abstract class RmArguments {

  @Parameter(shortName = 'r')
  abstract boolean recursive();

  @Parameter(shortName = 'f')
  abstract boolean force();

  @PositionalParameter
  abstract List<String> otherTokens();
}
