package net.zerobuilder.examples.gradle;


import net.jbock.CommandLineArguments;

import java.util.List;
import java.util.Optional;

final class NoName {

  final String message;
  final List<String> file;
  final boolean cmos;

  @CommandLineArguments
  NoName(Optional<String> message,
         List<String> file,
         boolean cmos) {
    this.message = message.orElse(null);
    this.file = file;
    this.cmos = cmos;
  }
}
