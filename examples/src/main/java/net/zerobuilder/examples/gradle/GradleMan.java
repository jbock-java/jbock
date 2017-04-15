package net.zerobuilder.examples.gradle;


import net.jbock.CommandLineArguments;
import net.jbock.Description;
import net.jbock.LongName;
import net.jbock.ShortName;

final class GradleMan {

  final String message;
  final String file;
  final String dir;
  final boolean cmos;

  @CommandLineArguments
  GradleMan(@LongName("message")
            @ShortName("m")
            @Description(lines = "The message")
                String message,
            @ShortName("f")
            @Description(lines = "The file")
                String file,
            @Description(lines = "The dir")
                String dir,
            @ShortName("c")
            @Description(lines = "Is it cmos?") boolean cmos) {
    this.message = message;
    this.file = file;
    this.dir = dir;
    this.cmos = cmos;
  }
}
