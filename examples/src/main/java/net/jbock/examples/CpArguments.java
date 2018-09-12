package net.jbock.examples;

import net.jbock.CommandLineArguments;
import net.jbock.Parameter;
import net.jbock.PositionalParameter;

@CommandLineArguments
abstract class CpArguments {

  @Parameter(shortName = 'r')
  abstract boolean recursive();

  @PositionalParameter
  abstract String source();

  @PositionalParameter
  abstract String dest();
}
