package net.jbock.examples;

import java.util.List;
import net.jbock.CommandLineArguments;
import net.jbock.Positional;
import net.jbock.ShortName;

@CommandLineArguments
abstract class RmArguments {

  @ShortName('r')
  abstract boolean recursive();

  @ShortName('f')
  abstract boolean force();

  @Positional
  abstract List<String> otherTokens();

  @Positional
  abstract List<String> ddTokens();
}
