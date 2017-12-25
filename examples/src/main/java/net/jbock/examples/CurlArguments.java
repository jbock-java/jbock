package net.jbock.examples;

import java.util.List;
import java.util.Optional;
import net.jbock.CommandLineArguments;
import net.jbock.Description;
import net.jbock.LongName;
import net.jbock.Positional;
import net.jbock.ShortName;

@CommandLineArguments(
    programName = "curl",
    missionStatement = "transfer a URL",
    overview = {
        "curl  is  a  tool  to  transfer data from or to a server " +
            "using one of the supported protocols",
        "",
        "curl offers a busload of useful tricks",
        "",
        "curl is powered by libcurl for all transfer-related features. " +
            "See libcurl(3) for details."
    })
abstract class CurlArguments {

  @ShortName('X')
  @LongName("request")
  @Description("Optional<String> for regular arguments")
  abstract Optional<String> method();

  @ShortName('H')
  @LongName("")
  @Description("List<String> for repeatable arguments")
  abstract List<String> headers();

  @ShortName('v')
  @LongName("")
  @Description("boolean for flags")
  abstract boolean verbose();

  @ShortName('i')
  abstract boolean include();

  @Positional
  abstract List<String> urls();
}
