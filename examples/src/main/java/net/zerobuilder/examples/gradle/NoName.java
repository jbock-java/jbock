package net.zerobuilder.examples.gradle;


import net.jbock.CommandLineArguments;

import java.util.List;

final class NoName {

  final String message;
  final List<String> file;
  final boolean cmos;

  @CommandLineArguments
  NoName(String message,
         List<String> file,
         boolean cmos) {
    this.message = message;
    this.file = file;
    this.cmos = cmos;
  }
}
