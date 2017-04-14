package net.zerobuilder.examples.gradle;


import net.jbock.CommandLineArguments;
import net.jbock.LongName;

final class GradleMan {

  final String message;

  @CommandLineArguments
  GradleMan(@LongName("message") String message) {
    this.message = message;
  }
}
