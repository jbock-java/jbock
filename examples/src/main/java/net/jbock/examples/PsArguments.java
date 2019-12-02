package net.jbock.examples;

import net.jbock.CLI;
import net.jbock.Option;

import java.util.Optional;

@CLI
abstract class PsArguments {

  @Option(value = "all", mnemonic = 'a')
  abstract boolean all();

  /**
   * This is the description.
   */
  @Option(value = "width", mnemonic = 'w')
  abstract Optional<Integer> width();
}
