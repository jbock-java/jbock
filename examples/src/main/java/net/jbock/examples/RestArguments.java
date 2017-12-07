package net.jbock.examples;

import java.util.List;
import net.jbock.CommandLineArguments;
import net.jbock.EverythingAfter;

@CommandLineArguments(allowGrouping = true)
abstract class RestArguments {

  abstract List<String> file();

  @EverythingAfter("-")
  abstract List<String> rest();
}
