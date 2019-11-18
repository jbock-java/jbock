package net.jbock.examples;

import net.jbock.CommandLineArguments;
import net.jbock.PositionalParameter;

@CommandLineArguments
abstract class MvArguments {

  @PositionalParameter(1)
  abstract String source();

  @PositionalParameter(2)
  abstract String dest();
}
