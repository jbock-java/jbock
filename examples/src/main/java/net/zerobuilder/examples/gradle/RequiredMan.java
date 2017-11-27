package net.zerobuilder.examples.gradle;


import java.util.List;
import net.jbock.CommandLineArguments;
import net.jbock.OtherTokens;

@CommandLineArguments(grouping = true)
abstract class RequiredMan {

  abstract String dir();

  @OtherTokens
  abstract List<String> otherTokens();
}
