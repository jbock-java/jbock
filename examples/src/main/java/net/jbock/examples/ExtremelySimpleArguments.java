package net.jbock.examples;

import net.jbock.Command;
import net.jbock.Parameter;

import java.util.OptionalInt;

@Command(helpEnabled = false)
abstract class ExtremelySimpleArguments {

  @Parameter(index = 0)
  abstract OptionalInt hello();
}
