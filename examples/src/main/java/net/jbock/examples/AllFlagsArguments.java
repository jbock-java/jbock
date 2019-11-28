package net.jbock.examples;

import net.jbock.CommandLineArguments;
import net.jbock.Parameter;

@CommandLineArguments
abstract class AllFlagsArguments {

  @Parameter(value = "smallFlag")
  abstract boolean smallFlag();

  @Parameter(value = "bigFlag")
  abstract Boolean bigFlag();
}
