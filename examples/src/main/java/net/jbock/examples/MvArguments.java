package net.jbock.examples;

import net.jbock.CLI;
import net.jbock.Param;

@CLI
abstract class MvArguments {

  @Param(1)
  abstract String source();

  @Param(2)
  abstract String dest();
}
