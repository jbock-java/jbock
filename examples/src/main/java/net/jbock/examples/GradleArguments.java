package net.jbock.examples;


import net.jbock.CommandLineArguments;
import net.jbock.Parameter;
import net.jbock.PositionalParameter;

import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

@CommandLineArguments
abstract class GradleArguments {

  /**
   * the message
   * message goes here
   */
  @Parameter(shortName = 'm')
  abstract Optional<String> message();

  /**
   * the files
   */
  @Parameter(shortName = 'f')
  abstract List<String> file();

  /**
   * the dir
   */
  @Parameter
  abstract Optional<String> dir();

  /**
   * cmos flag
   */
  @Parameter(shortName = 'c', longName = "")
  abstract boolean cmos();

  @Parameter(shortName = 'v')
  abstract boolean verbose();

  @PositionalParameter
  abstract List<String> otherTokens();

  @CommandLineArguments
  static abstract class Foo {

    @Parameter
    abstract OptionalInt bar();
  }

  @CommandLineArguments
  static abstract class Bar {

    @Parameter
    abstract List<String> bar();
  }
}
