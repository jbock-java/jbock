package net.zerobuilder.examples.gradle;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Optional;
import org.junit.Test;

public class CurlArgumentsTest {

  @Test
  public void testPrintUsage() throws Exception {
    CurlArguments args = CurlArguments_Parser.parse(
        new String[]{"-H'Content-Type: application/json'", "-v", "http://localhost:8080"});
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    CurlArguments_Parser.printUsage(new PrintStream(out), 2);
    String[] lines = new String(out.toByteArray()).split("\n", -1);
    assertThat(lines.length).isEqualTo(10);
    assertThat(lines[0]).isEqualTo("-X, --method=VAL");
    assertThat(lines[1]).isEqualTo("  Optional<String> for regular arguments");
    assertThat(lines[2]).isEqualTo("-H, --header=VAL");
    assertThat(lines[3]).isEqualTo("  List<String> for repeatable arguments");
    assertThat(lines[4]).isEqualTo("-v, --verbose");
    assertThat(lines[5]).isEqualTo("  boolean for flags");
    assertThat(lines[6]).isEqualTo("[urls]");
    assertThat(lines[7]).isEqualTo("  @OtherTokens to capture everything else.");
    assertThat(lines[8]).isEqualTo("  In this case, everything that isn't '-v' or follows '-H' or '-X'");
    assertThat(lines[9]).isEqualTo("");
    assertThat(args.urls().size()).isEqualTo(1);
    assertThat(args.urls().get(0)).isEqualTo("http://localhost:8080");
  }

  @Test
  public void testEmptyNonRepeatable() {
    assertThat(CurlArguments_Parser.parse(new String[]{}).method())
        .isEqualTo(Optional.empty());
    assertThat(CurlArguments_Parser.parse(new String[]{"--method="}).method())
        .isEqualTo(Optional.of(""));
    assertThat(CurlArguments_Parser.parse(new String[]{"--method", ""}).method())
        .isEqualTo(Optional.of(""));
    assertThat(CurlArguments_Parser.parse(new String[]{"--method="}).method())
        .isEqualTo(Optional.of(""));
  }

  @Test
  public void testEmptyRepeatable() {
    assertThat(CurlArguments_Parser.parse(new String[]{}).headers())
        .isEqualTo(emptyList());
    assertThat(CurlArguments_Parser.parse(new String[]{"--header="}).headers())
        .isEqualTo(singletonList(""));
    assertThat(CurlArguments_Parser.parse(new String[]{"--header", ""}).headers())
        .isEqualTo(singletonList(""));
    assertThat(CurlArguments_Parser.parse(new String[]{"--header="}).headers())
        .isEqualTo(singletonList(""));
  }

  @Test(expected = IllegalArgumentException.class)
  public void errorMissingRepeatable() {
    CurlArguments_Parser.parse(new String[]{"--header"});
  }

  @Test(expected = IllegalArgumentException.class)
  public void errorMissingNonRepeatable() {
    CurlArguments_Parser.parse(new String[]{"--method"});
  }
}
