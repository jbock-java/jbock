package net.jbock.examples;

import net.jbock.CommandLineArguments;
import net.jbock.PositionalParameter;

@CommandLineArguments
abstract class FewerPositionalArguments {

  @PositionalParameter
  abstract String source();

  @PositionalParameter
  abstract String dest();
}
