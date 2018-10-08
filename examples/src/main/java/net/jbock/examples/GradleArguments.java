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
  @Parameter(optional = true, shortName = 'm')
  abstract Optional<String> message();

  /**
   * the files
   */
  @Parameter(repeatable = true, shortName = 'f')
  abstract List<String> file();

  /**
   * the dir
   */
  @Parameter(optional = true)
  abstract Optional<String> dir();

  /**
   * cmos flag
   */
  @Parameter(flag = true, shortName = 'c', longName = "")
  abstract boolean cmos();

  @Parameter(flag = true, shortName = 'v')
  abstract boolean verbose();

  @PositionalParameter(repeatable = true)
  abstract List<String> otherTokens();

  @CommandLineArguments
  static abstract class Foo {

    @Parameter(optional = true)
    abstract OptionalInt bar();
  }

  @CommandLineArguments
  static abstract class Bar {

    @Parameter(repeatable = true)
    abstract List<String> bar();
  }
}
