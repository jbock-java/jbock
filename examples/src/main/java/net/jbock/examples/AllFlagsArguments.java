package net.jbock.examples;

import net.jbock.Command;
import net.jbock.Option;

@Command
abstract class AllFlagsArguments {

  @Option("smallFlag")
  abstract boolean smallFlag();

  @Option("bigFlag")
  abstract Boolean bigFlag();
}
