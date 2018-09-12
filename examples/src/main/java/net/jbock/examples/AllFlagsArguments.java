package net.jbock.examples;

import net.jbock.CommandLineArguments;
import net.jbock.Parameter;

@CommandLineArguments
abstract class AllFlagsArguments {

  @Parameter
  abstract boolean smallFlag();

  @Parameter
  abstract Boolean bigFlag();
}
