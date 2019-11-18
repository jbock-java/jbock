package net.jbock.examples;

import net.jbock.CommandLineArguments;
import net.jbock.Parameter;

import java.util.Optional;

@CommandLineArguments
abstract class PsArguments {

  @Parameter(value = "all", mnemonic = 'a')
  abstract boolean all();

  /**
   * This is the description.
   */
  @Parameter(value = "width", mnemonic = 'w')
  abstract Optional<Integer> width();
}
