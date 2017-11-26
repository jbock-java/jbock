package net.zerobuilder.examples.gradle;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class SimpleArgumentsTest {

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  @Test
  public void noGrouping() {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("Unknown token: xf");
    SimpleArguments_Parser.parse(new String[]{"xf", "1"});
  }

  @Test
  public void noGroupingHyphen() {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("Invalid characters after flag in: -xf");
    SimpleArguments_Parser.parse(new String[]{"-xf", "1"});
  }
}
