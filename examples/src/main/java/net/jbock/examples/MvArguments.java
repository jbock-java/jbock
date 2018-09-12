package net.jbock.examples;

import net.jbock.CommandLineArguments;
import net.jbock.PositionalParameter;

@CommandLineArguments
abstract class MvArguments {

  @PositionalParameter
  abstract String source();

  @PositionalParameter
  abstract String dest();
}
