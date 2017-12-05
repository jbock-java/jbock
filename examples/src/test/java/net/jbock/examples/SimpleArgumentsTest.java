package net.jbock.examples;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class SimpleArgumentsTest {

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  @Test
  public void noGroupingOfFirstArgument() {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("Unknown token: xf");
    SimpleArguments_Parser.parse(new String[]{"xf", "1"});
  }

  @Test
  public void noGroupingFirstArgumentHyphen() {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("Unknown token: -xf");
    SimpleArguments_Parser.parse(new String[]{"-xf", "1"});
  }
}
