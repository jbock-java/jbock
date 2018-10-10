package net.jbock.examples.fixture;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class TestOutputStream {

  private final ByteArrayOutputStream baos = new ByteArrayOutputStream();

  public final PrintStream out = new PrintStream(baos);

  @Override
  public String toString() {
    return new String(baos.toByteArray());
  }
}
