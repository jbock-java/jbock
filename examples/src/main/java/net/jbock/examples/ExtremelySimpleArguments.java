package net.jbock.examples;

import net.jbock.Command;
import net.jbock.Parameter;
import net.jbock.Parameters;

import java.util.List;
import java.util.OptionalInt;

@Command(helpEnabled = false)
abstract class ExtremelySimpleArguments {

  @Parameters
  abstract List<String> hello();
}
