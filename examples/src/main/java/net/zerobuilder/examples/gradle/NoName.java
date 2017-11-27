package net.zerobuilder.examples.gradle;


import net.jbock.CommandLineArguments;

import java.util.List;
import java.util.Optional;

@CommandLineArguments(grouping = true)
abstract class NoName {

  abstract Optional<String> message();

  abstract List<String> file();

  abstract boolean cmos();
}
