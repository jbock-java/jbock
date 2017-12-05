package net.jbock.examples;


import java.util.List;
import java.util.Optional;
import net.jbock.CommandLineArguments;

@CommandLineArguments
abstract class SimpleNoName {

  abstract Optional<String> message();

  abstract List<String> file();

  abstract boolean cmos();
}
