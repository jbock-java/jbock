package net.zerobuilder.examples.gradle;


import net.jbock.ArgumentName;
import net.jbock.CommandLineArguments;
import net.jbock.Description;
import net.jbock.LongName;
import net.jbock.ShortName;

import java.io.IOException;
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
            @ArgumentName("MESSAGE")
            @Description({"the message", "message goes here"})
                String message,
            @ShortName('f')
            @Description("the files")
            @ArgumentName("FILE")
                List<String> file,
            @Description("the dir")
            @ArgumentName("DIR")
                String dir,
            @ShortName('c')
            @Description("cmos flag")
                boolean cmos) throws IOException, NullPointerException {
    this.message = message;
    this.file = file;
    this.dir = dir;
    this.cmos = cmos;
  }
}
