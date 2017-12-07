package net.jbock.examples;

import java.util.List;
import net.jbock.CommandLineArguments;
import net.jbock.Positional;
import net.jbock.ShortName;

@CommandLineArguments(allowGrouping = true)
abstract class RmArguments {

  @ShortName('r')
  abstract boolean recursive();

  @ShortName('f')
  abstract boolean force();

  @Positional
  abstract List<String> otherTokens();
}
