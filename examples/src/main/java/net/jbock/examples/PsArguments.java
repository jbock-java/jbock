package net.jbock.examples;

import net.jbock.CommandLineArguments;
import net.jbock.Parameter;

import java.util.OptionalInt;

@CommandLineArguments
abstract class PsArguments {

  @Parameter(shortName = 'a')
  abstract boolean all();

  /**
   * @return number A number
   */
  @Parameter(shortName = 'w')
  abstract OptionalInt wide();
}
