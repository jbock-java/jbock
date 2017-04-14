package net.zerobuilder.examples.gradle;


import net.jbock.CommandLineArguments;
import net.jbock.LongName;
import net.jbock.ShortName;

final class GradleMan {

  final String message;

  @CommandLineArguments
  GradleMan(@LongName("message") @ShortName("m") String message) {
    this.message = message;
  }
}
