package net.jbock.examples;

import net.jbock.Command;
import net.jbock.Option;

import java.util.Optional;

@Command
abstract class PsArguments {

  @Option(names = {"--all", "-a"})
  abstract boolean all();

  /**
   * This is the description.
   */
  @Option(names = {"--width", "-w"})
  abstract Optional<Integer> width();
}
