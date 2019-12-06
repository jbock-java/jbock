package net.jbock.examples;

import net.jbock.Command;
import net.jbock.Param;

@Command
abstract class MvArguments {

  @Param(1)
  abstract String source();

  @Param(2)
  abstract String dest();
}
