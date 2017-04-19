package net.zerobuilder.examples.gradle;


import net.jbock.CommandLineArguments;
import net.jbock.Description;
import net.jbock.LongName;
import net.jbock.ShortName;

import java.util.List;

final class GradleMan {

  final String message;
  final List<String> file;
  final String dir;
  final boolean cmos;

  static final class Foo {
    final String bar;

    @CommandLineArguments
    Foo(String bar) {
      this.bar = bar;
    }
  }

  @CommandLineArguments
  GradleMan(@LongName("message")
            @ShortName('m')
            @Description(lines = {"the message", "message goes here"}, argumentName = "MESSAGE")
                String message,
            @ShortName('f')
            @Description(lines = "the files", argumentName = "FILE")
                List<String> file,
            @Description(lines = "the dir", argumentName = "DIR")
                String dir,
            @ShortName('c')
            @Description(lines = "cmos flag") boolean cmos) {
    this.message = message;
    this.file = file;
    this.dir = dir;
    this.cmos = cmos;
  }
}
