package net.jbock.examples;

import net.jbock.CommandLineArguments;
import net.jbock.Parameter;

import java.util.OptionalInt;

@CommandLineArguments
abstract class PsArguments {

  @Parameter(
      flag = true,
      longName = "all",
      shortName = 'a')
  abstract boolean all();

  /**
   * This is the description.
   */
  @Parameter(
      optional = true,
      longName = "width",
      shortName = 'w',
      argHandle = "number")
  abstract OptionalInt width();
}
