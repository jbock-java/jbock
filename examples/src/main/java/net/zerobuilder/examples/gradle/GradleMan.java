package net.zerobuilder.examples.gradle;


import java.util.List;
import java.util.Optional;
import net.jbock.ArgumentName;
import net.jbock.CommandLineArguments;
import net.jbock.Description;
import net.jbock.OtherTokens;
import net.jbock.ShortName;
import net.jbock.SuppressLongName;

@CommandLineArguments
abstract class GradleMan {

  @ShortName('m')
  @ArgumentName("MESSAGE")
  @Description({"the message", "message goes here"})
  abstract Optional<String> message();

  @ShortName('f')
  @Description("the files")
  @ArgumentName("FILE")
  abstract List<String> file();

  @Description("the dir")
  @ArgumentName("DIR")
  abstract Optional<String> dir();

  @ShortName('c')
  @SuppressLongName
  @Description("cmos flag")
  abstract boolean cmos();

  @ShortName('v')
  abstract boolean verbose();

  @OtherTokens
  abstract List<String> otherTokens();

  @CommandLineArguments
  static abstract class Foo {
    abstract Optional<String> bar();
  }
}
