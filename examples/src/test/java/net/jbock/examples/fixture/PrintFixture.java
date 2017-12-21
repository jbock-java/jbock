package net.jbock.examples.fixture;

import static org.junit.Assert.assertArrayEquals;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.function.ObjIntConsumer;

public final class PrintFixture<E> {

  private final ObjIntConsumer<PrintStream> print;

  private PrintFixture(ObjIntConsumer<PrintStream> print) {
    this.print = print;
  }

  public static <E> PrintFixture<E> printFixture(ObjIntConsumer<PrintStream> print) {
    return new PrintFixture<>(print);
  }

  public void assertPrints(String... expected) {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    print.accept(new PrintStream(out), 2);
    String[] actual = new String(out.toByteArray()).split("\\r?\\n", -1);
    assertArrayEquals(expected, actual);
  }
}
