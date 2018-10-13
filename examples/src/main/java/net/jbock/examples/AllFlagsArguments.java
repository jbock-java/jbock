package net.jbock.examples;

import net.jbock.CommandLineArguments;
import net.jbock.Parameter;

@CommandLineArguments
abstract class AllFlagsArguments {

  @Parameter(
      longName = "smallFlag",
      flag = true)
  abstract boolean smallFlag();

  @Parameter(
      longName = "bigFlag",
      flag = true)
  abstract Boolean bigFlag();
}
