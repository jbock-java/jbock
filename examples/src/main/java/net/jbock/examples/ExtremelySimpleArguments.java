package net.jbock.examples;

import net.jbock.CommandLineArguments;
import net.jbock.PositionalParameter;

@CommandLineArguments
abstract class ExtremelySimpleArguments {

  @PositionalParameter
  abstract int hello();
}
