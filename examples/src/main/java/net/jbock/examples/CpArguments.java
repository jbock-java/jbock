package net.jbock.examples;

import net.jbock.CommandLineArguments;
import net.jbock.Parameter;
import net.jbock.PositionalParameter;

import java.util.Optional;

@CommandLineArguments
abstract class CpArguments {

  @PositionalParameter
  abstract String source();

  @PositionalParameter
  abstract String dest();

  @Parameter(shortName = 'r')
  abstract boolean recursive();

  /**
   * Override the usual backup suffix
   */
  @Parameter(shortName = 's')
  abstract Optional<String> suffix();
}
