package net.jbock.examples;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class SimpleRmArgumentsTest {

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  @Test
  public void testRest() {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("Option '-f' is not repeatable");
    SimpleRmArguments_Parser.parse(new String[]{"-f", "a", "--", "-r", "--", "-f"});
  }
}
