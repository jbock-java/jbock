package net.jbock.examples;

import net.jbock.CLI;
import net.jbock.Param;

@CLI
abstract class ExtremelySimpleArguments {

  @Param(value = 1)
  abstract int hello();
}
