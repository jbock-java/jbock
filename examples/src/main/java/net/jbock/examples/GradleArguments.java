package net.jbock.examples;


import net.jbock.CommandLineArguments;
import net.jbock.Parameter;
import net.jbock.PositionalParameter;

import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

@CommandLineArguments(allowEscapeSequence = true)
abstract class GradleArguments {

  /**
   * the message
   * message goes here
   */
  @Parameter(
      optional = true,
      longName = "message",
      shortName = 'm')
  abstract Optional<String> message();

  /**
   * the files
   */
  @Parameter(
      repeatable = true,
      longName = "file",
      shortName = 'f')
  abstract List<String> file();

  /**
   * the dir
   */
  @Parameter(
      longName = "dir",
      optional = true)
  abstract Optional<String> dir();

  /**
   * cmos flag
   */
  @Parameter(
      flag = true,
      shortName = 'c')
  abstract boolean cmos();

  @Parameter(
      flag = true,
      longName = "verbose",
      shortName = 'v')
  abstract boolean verbose();

  @PositionalParameter(repeatable = true)
  abstract List<String> otherTokens();

  @CommandLineArguments
  static abstract class Foo {

    @Parameter(
        longName = "bar",
        optional = true)
    abstract OptionalInt bar();
  }

  @CommandLineArguments
  static abstract class Bar {

    @Parameter(
        longName = "bar",
        repeatable = true)
    abstract List<String> bar();
  }
}
