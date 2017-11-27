package net.zerobuilder.examples.gradle;

import java.util.List;
import net.jbock.CommandLineArguments;
import net.jbock.EverythingAfter;

@CommandLineArguments
abstract class SimpleRestArguments {

  abstract List<String> file();

  @EverythingAfter("-")
  abstract List<String> rest();
}
