package net.jbock.examples;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class NullPointerTest {

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  @Test
  public void errorNullInArray() {
    exception.expect(NullPointerException.class);
    GradleArguments_Parser.parse(new String[]{null});
  }

  @Test
  public void errorArrayIsNull() {
    exception.expect(NullPointerException.class);
    String[] args = null;
    GradleArguments_Parser.parse(args);
  }
}
