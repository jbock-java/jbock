package net.jbock.examples;

import net.jbock.Command;
import net.jbock.Option;
import net.jbock.Param;

import java.util.Optional;

@Command
abstract class CpArguments {

  enum Control {
    NONE,
    NUMBERED,
    EXISTING,
    SIMPLE
  }

  @Param(0)
  abstract String source();

  @Param(1)
  abstract String dest();

  @Option(names = {"--r", "-r"})
  abstract boolean recursive();

  @Option(names = "--backup")
  abstract Optional<Control> backup();


  /**
   * Override the usual backup suffix
   */
  @Option(names = {"--s", "-s"})
  abstract Optional<String> suffix();
}
