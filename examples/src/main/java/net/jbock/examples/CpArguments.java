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

  @PositionalParameter
  abstract String source();

  @PositionalParameter(position = 1)
  abstract String dest();

  @Parameter(flag = true, shortName = 'r')
  abstract boolean recursive();

  @Parameter(longName = "backup", descriptionArgumentName = "CONTROL")
  abstract Optional<Control> backup();


  /**
   * Override the usual backup suffix
   */
  @Parameter(shortName = 's')
  abstract Optional<String> suffix();
}
