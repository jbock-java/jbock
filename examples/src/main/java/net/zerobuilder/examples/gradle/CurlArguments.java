package net.zerobuilder.examples.gradle;

import java.util.List;
import java.util.Optional;
import net.jbock.CommandLineArguments;
import net.jbock.Description;
import net.jbock.OtherTokens;
import net.jbock.ShortName;

@CommandLineArguments
abstract class CurlArguments {

  @ShortName('X')
  @Description("Optional<String> for regular arguments")
  abstract Optional<String> method();

  @ShortName('H')
  @Description("List<String> for repeatable arguments")
  abstract List<String> headers();

  @ShortName('v')
  @Description("boolean for flags")
  abstract boolean verbose();

  @OtherTokens
  @Description({
      "@OtherTokens to capture everything else.",
      "In this case, everything that isn't '-v' or follows '-H' or '-X'"})
  abstract List<String> urls();
}
