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

  @Param(1)
  abstract String source();

  @Param(2)
  abstract String dest();

  @Option(value = "r", mnemonic = 'r')
  abstract boolean recursive();

  @Option("backup")
  abstract Optional<Control> backup();


  /**
   * Override the usual backup suffix
   */
  @Option(value = "s", mnemonic = 's')
  abstract Optional<String> suffix();
}
