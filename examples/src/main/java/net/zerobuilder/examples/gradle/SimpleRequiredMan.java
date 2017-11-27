package net.zerobuilder.examples.gradle;


import java.util.List;
import net.jbock.CommandLineArguments;
import net.jbock.OtherTokens;

@CommandLineArguments
abstract class SimpleRequiredMan {

  abstract String dir();

  @OtherTokens
  abstract List<String> otherTokens();
}
