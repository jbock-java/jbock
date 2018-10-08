package net.jbock.examples;

import net.jbock.CommandLineArguments;
import net.jbock.Parameter;

@CommandLineArguments
abstract class AllFlagsArguments {

  @Parameter(flag = true)
  abstract boolean smallFlag();

  @Parameter(flag = true)
  abstract Boolean bigFlag();
}
