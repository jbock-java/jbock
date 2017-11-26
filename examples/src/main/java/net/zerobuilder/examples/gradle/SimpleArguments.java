package net.zerobuilder.examples.gradle;

import java.util.Optional;
import net.jbock.CommandLineArguments;
import net.jbock.ShortName;

@CommandLineArguments(grouping = false)
abstract class SimpleArguments {

  @ShortName('x')
  abstract boolean extract();

  abstract Optional<String> file();
}
