package net.zerobuilder.examples.gradle;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.StringWriter;
import org.junit.Test;

public class CurlArgumentsTest {

  @Test
  public void testRemaining() throws Exception {
    CurlArguments curl = CurlArguments_Parser.parse(
        new String[]{"-H'Content-Type: application/json'", "-v", "http://localhost:8080"});
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    CurlArguments_Parser.printUsage(new PrintStream(out), 2);
    String[] lines = new String(out.toByteArray()).split("\n", -1);
    assertThat(lines.length, is(10));
    assertThat(lines[0], is("-X VAL"));
    assertThat(lines[1], is("  Optional<String> for regular arguments"));
    assertThat(lines[2], is("-H VAL"));
    assertThat(lines[3], is("  List<String> for repeatable arguments"));
    assertThat(lines[4], is("-v"));
    assertThat(lines[5], is("  boolean for flags"));
    assertThat(lines[6], is("[urls]"));
    assertThat(lines[7], is("  @OtherTokens to capture everything else."));
    assertThat(lines[8], is("  In this case, everything that isn't '-v' or follows '-H' or '-X'"));
    assertThat(lines[9], is(""));
    assertThat(curl.urls().size(), is(1));
    assertThat(curl.urls().get(0), is("http://localhost:8080"));
  }
}
