package net.jbock.examples;

import net.jbock.CommandLineArguments;
import net.jbock.LongName;
import net.jbock.Positional;
import net.jbock.ShortName;

import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

@CommandLineArguments
abstract class AllIntegersArguments {

  @Positional
  abstract List<Integer> positional();

  @ShortName('i')
  abstract List<Integer> listOfIntegers();

  @LongName("opt")
  abstract Optional<Integer> optionalInteger();

  @LongName("optint")
  abstract OptionalInt optionalInt();

  @LongName("obj")
  abstract Integer integer();

  @LongName("prim")
  abstract int primitiveInt();
}
