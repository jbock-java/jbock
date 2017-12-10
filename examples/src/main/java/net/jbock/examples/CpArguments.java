package net.jbock.examples;

import java.util.List;
import net.jbock.CommandLineArguments;
import net.jbock.Positional;

@CommandLineArguments
abstract class CpArguments {

  @Positional
  abstract String source();

  @Positional
  abstract String dest();

  @Positional
  abstract List<String> otherTokens();

  @Positional
  abstract List<String> ddTokens();
}
