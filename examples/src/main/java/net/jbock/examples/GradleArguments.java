package net.jbock.examples;


import net.jbock.CommandLineArguments;
import net.jbock.Parameter;
import net.jbock.PositionalParameter;

import java.util.List;
import java.util.Optional;

@CommandLineArguments
abstract class GradleArguments {

  /**
   * the message
   * message goes here
   */
  @Parameter(
      value = "message",
      mnemonic = 'm')
  abstract Optional<String> message();

  /**
   * the files
   */
  @Parameter(
      value = "file",
      mnemonic = 'f')
  abstract List<String> file();

  /**
   * the dir
   */
  @Parameter(value = "dir")
  abstract Optional<String> dir();

  /**
   * cmos flag
   */
  @Parameter(value = "c", mnemonic = 'c')
  abstract Boolean cmos();

  @Parameter(
      value = "verbose",
      mnemonic = 'v')
  abstract boolean verbose();

  @PositionalParameter(value = 1)
  abstract List<String> otherTokens();

  @CommandLineArguments
  static abstract class Foo {

    @Parameter(value = "bar")
    abstract Optional<Integer> bar();
  }

  @CommandLineArguments
  static abstract class Bar {

    @Parameter(value = "bar")
    abstract List<String> bar();
  }
}
