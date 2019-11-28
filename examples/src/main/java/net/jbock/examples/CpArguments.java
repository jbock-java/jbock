package net.jbock.examples;

import net.jbock.CommandLineArguments;
import net.jbock.Parameter;
import net.jbock.PositionalParameter;

import java.util.Optional;

@CommandLineArguments
abstract class CpArguments {

  enum Control {
    NONE,
    NUMBERED,
    EXISTING,
    SIMPLE
  }

  @PositionalParameter(value = 1)
  abstract String source();

  @PositionalParameter(value = 2)
  abstract String dest();

  @Parameter(value = "r", mnemonic = 'r')
  abstract boolean recursive();

  @Parameter(value = "backup")
  abstract Optional<Control> backup();


  /**
   * Override the usual backup suffix
   */
  @Parameter(value = "s", mnemonic = 's')
  abstract Optional<String> suffix();
}
