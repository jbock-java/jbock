package net.jbock.examples;

import net.jbock.CommandLineArguments;
import net.jbock.ShortName;

import java.util.List;

@CommandLineArguments
abstract class AllNumbersArguments {

  @ShortName('i')
  abstract List<Integer> listOfIntegers();
}
