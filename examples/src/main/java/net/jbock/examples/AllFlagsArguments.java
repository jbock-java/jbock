package net.jbock.examples;

import net.jbock.CommandLineArguments;

@CommandLineArguments
abstract class AllFlagsArguments {

  abstract boolean smallFlag();

  abstract Boolean bigFlag();
}
