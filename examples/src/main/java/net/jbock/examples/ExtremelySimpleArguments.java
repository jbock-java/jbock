package net.jbock.examples;

import net.jbock.Command;
import net.jbock.Param;

import java.util.OptionalInt;

@Command
abstract class ExtremelySimpleArguments {

  @Param(0)
  abstract OptionalInt hello();
}
