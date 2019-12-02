package net.jbock.examples;

import net.jbock.CLI;
import net.jbock.Option;

@CLI
abstract class AllFlagsArguments {

  @Option(value = "smallFlag")
  abstract boolean smallFlag();

  @Option(value = "bigFlag")
  abstract Boolean bigFlag();
}
