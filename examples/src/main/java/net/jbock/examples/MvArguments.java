package net.jbock.examples;

import net.jbock.Command;
import net.jbock.Parameter;

@Command(ansi = false)
interface MvArguments extends MvArguments_Parent {

  @Parameter(index = 1)
  String dest();
}
