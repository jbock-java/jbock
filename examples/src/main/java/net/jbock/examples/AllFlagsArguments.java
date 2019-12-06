package net.jbock.examples;

import net.jbock.Command;
import net.jbock.Option;

@Command
abstract class AllFlagsArguments {

  @Option(value = "smallFlag")
  abstract boolean smallFlag();

  @Option(value = "bigFlag")
  abstract Boolean bigFlag();
}
