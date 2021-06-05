package net.jbock.examples.fixture;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static net.jbock.examples.fixture.ParserTestFixture.assertArraysEquals;

public class TestOutputStream {

  private final ByteArrayOutputStream baos = new ByteArrayOutputStream();

  public final PrintStream out = new PrintStream(baos);

  @Override
  public String toString() {
    return baos.toString();
  }

  public void assertEquals(String... expected) {
    String stdout = baos.toString();
    String[] actual = stdout.split("\\R", -1);
    assertArraysEquals(expected, actual);
  }

  public String[] split() {
    String stdout = baos.toString();
    return stdout.split("\\R", -1);
  }
}
