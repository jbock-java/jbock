package net.jbock.examples;

import net.jbock.Command;
import net.jbock.Parameter;

import java.util.OptionalInt;

@Command
abstract class ExtremelySimpleArguments {

  @Parameter(index = 0)
  abstract OptionalInt hello();
}
