package net.jbock.examples;

import java.util.List;
import java.util.Optional;
import net.jbock.CommandLineArguments;
import net.jbock.LongName;
import net.jbock.Positional;
import net.jbock.ShortName;
import net.jbock.SuppressLongName;

@CommandLineArguments
abstract class SimpleCurlArguments {

  @ShortName('X')
  @LongName("request")
  abstract Optional<String> method();

  @ShortName('H')
  @SuppressLongName
  abstract List<String> headers();

  @ShortName('v')
  @SuppressLongName
  abstract boolean verbose();

  @Positional(esc = false)
  abstract List<String> urls();
}
