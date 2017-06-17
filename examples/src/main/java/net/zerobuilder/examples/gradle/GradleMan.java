package net.zerobuilder.examples.gradle;


import net.jbock.ArgumentName;
import net.jbock.CommandLineArguments;
import net.jbock.Description;
import net.jbock.LongName;
import net.jbock.ShortName;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

final class GradleMan {

  final String message;
  final List<String> file;
  final String dir;
  final boolean cmos;
  final boolean verbose;

  static final class Foo {
    final String bar;

    @CommandLineArguments
    Foo(Optional<String> bar) {
      this.bar = bar.orElse(null);
    }
  }

  @CommandLineArguments
  GradleMan(@LongName("message")
            @ShortName('m')
            @ArgumentName("MESSAGE")
            @Description({"the message", "message goes here"})
                Optional<String> message,
            @ShortName('f')
            @Description("the files")
            @ArgumentName("FILE")
                List<String> file,
            @Description("the dir")
            @ArgumentName("DIR")
                Optional<String> dir,
            @ShortName('c')
            @Description("cmos flag")
                boolean cmos,
            @ShortName('v')
                boolean verbose) throws IOException, NullPointerException {
    this.message = message.orElse(null);
    this.file = file;
    this.dir = dir.orElse(null);
    this.cmos = cmos;
    this.verbose = verbose;
  }
}
