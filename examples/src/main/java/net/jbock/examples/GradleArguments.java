package net.jbock.examples;


import net.jbock.CommandLineArguments;
import net.jbock.Parameter;
import net.jbock.PositionalParameter;

import java.util.List;
import java.util.Optional;

@CommandLineArguments(allowEscapeSequence = true)
abstract class GradleArguments {

  /**
   * the message
   * message goes here
   */
  @Parameter(
      longName = "message",
      shortName = 'm')
  abstract Optional<String> message();

  /**
   * the files
   */
  @Parameter(
      longName = "file",
      shortName = 'f')
  abstract List<String> file();

  /**
   * the dir
   */
  @Parameter(
      longName = "dir")
  abstract Optional<String> dir();

  /**
   * cmos flag
   */
  @Parameter(
      shortName = 'c')
  abstract Boolean cmos();

  @Parameter(
      longName = "verbose",
      shortName = 'v')
  abstract boolean verbose();

  @PositionalParameter
  abstract List<String> otherTokens();

  @CommandLineArguments
  static abstract class Foo {

    @Parameter(
        longName = "bar",
        optional = true)
    abstract Optional<Integer> bar();
  }

  @CommandLineArguments
  static abstract class Bar {

    @Parameter(
        longName = "bar",
        repeatable = true)
    abstract List<String> bar();
  }
}
