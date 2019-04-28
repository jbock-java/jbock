package net.jbock.examples;

import net.jbock.CommandLineArguments;
import net.jbock.Parameter;

@CommandLineArguments
abstract class AllFlagsArguments {

  @Parameter(longName = "smallFlag")
  abstract boolean smallFlag();

  @Parameter(longName = "bigFlag")
  abstract Boolean bigFlag();
}
