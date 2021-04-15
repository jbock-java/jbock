package net.jbock.examples;

import net.jbock.Command;
import net.jbock.Param;

@Command
abstract class ExtremelySimpleArguments {

  @Param(0)
  abstract int hello();
}
