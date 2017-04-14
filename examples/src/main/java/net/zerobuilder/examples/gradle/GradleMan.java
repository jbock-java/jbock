package net.zerobuilder.examples.gradle;


import net.jbock.CommandLineArguments;
import net.jbock.Description;
import net.jbock.Flag;
import net.jbock.LongName;
import net.jbock.ShortName;

final class GradleMan {

  final String message;
  final String cmos;
  final String file;
  final String dir;

  @CommandLineArguments
  GradleMan(@LongName("message")
            @ShortName("m")
            @Description("The message")
                String message,
            @ShortName("f")
            @Description("The file")
                String file,
            @Description("The dir")
                String dir,
            @ShortName("c")
            @Description("Is it cmos?")
            @Flag String cmos) {
    this.message = message;
    this.cmos = cmos;
    this.file = file;
    this.dir = dir;
  }
}
