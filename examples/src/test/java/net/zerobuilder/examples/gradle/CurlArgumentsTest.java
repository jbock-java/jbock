package net.zerobuilder.examples.gradle;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Optional;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class CurlArgumentsTest {

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  @Test
  public void testNonRepeatable() {
    assertThat(CurlArguments_Parser.parse(new String[]{}).method())
        .isEqualTo(Optional.empty());
    assertThat(CurlArguments_Parser.parse(new String[]{"--method="}).method())
        .isEqualTo(Optional.of(""));
    assertThat(CurlArguments_Parser.parse(new String[]{"--method= "}).method())
        .isEqualTo(Optional.of(" "));
    assertThat(CurlArguments_Parser.parse(new String[]{"--method", ""}).method())
        .isEqualTo(Optional.of(""));
    assertThat(CurlArguments_Parser.parse(new String[]{"-X1"}).method())
        .isEqualTo(Optional.of("1"));
    assertThat(CurlArguments_Parser.parse(new String[]{"-X", "1"}).method())
        .isEqualTo(Optional.of("1"));
  }

  @Test
  public void testRepeatable() {
    assertThat(CurlArguments_Parser.parse(new String[]{}).headers())
        .isEqualTo(emptyList());
    assertThat(CurlArguments_Parser.parse(new String[]{"--header="}).headers())
        .isEqualTo(singletonList(""));
    assertThat(CurlArguments_Parser.parse(new String[]{"--header= "}).headers())
        .isEqualTo(singletonList(" "));
    assertThat(CurlArguments_Parser.parse(new String[]{"--header", ""}).headers())
        .isEqualTo(singletonList(""));
    assertThat(CurlArguments_Parser.parse(new String[]{"-H1"}).headers())
        .isEqualTo(singletonList("1"));
    assertThat(CurlArguments_Parser.parse(new String[]{"-H", "1"}).headers())
        .isEqualTo(singletonList("1"));
  }

  @Test
  public void testGrouping() {
    assertThat(CurlArguments_Parser.parse(new String[]{"-vH1"}).headers())
        .isEqualTo(singletonList("1"));
    assertThat(CurlArguments_Parser.parse(new String[]{"-vH1"}).verbose())
        .isTrue();
    assertThat(CurlArguments_Parser.parse(new String[]{"-vH1", "-H2"}).headers())
        .isEqualTo(asList("1", "2"));
    assertThat(CurlArguments_Parser.parse(new String[]{"-vH1", "-H2"}).verbose())
        .isTrue();
    assertThat(CurlArguments_Parser.parse(new String[]{"-vXPOST"}).method())
        .isEqualTo(Optional.of("POST"));
    assertThat(CurlArguments_Parser.parse(new String[]{"-vXPOST"}).verbose())
        .isTrue();
  }

  @Test
  public void errorGroupingDuplicateFlag() {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("In option group -vH'Content-Type: application/xml': option VERBOSE is not repeatable");
    CurlArguments_Parser.parse(new String[]{"-v", "-vH'Content-Type: application/xml'"});
  }

  @Test
  public void errorMissingRepeatable() {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("Missing value after token: --header");
    CurlArguments_Parser.parse(new String[]{"--header"});
  }

  @Test
  public void errorMissingNonRepeatable() {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("Missing value after token: --method");
    CurlArguments_Parser.parse(new String[]{"--method"});
  }

  @Test
  public void errorDuplicateNonRepeatableLong() {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("Found token: --method, but option METHOD is not repeatable");
    CurlArguments_Parser.parse(new String[]{"--method", "GET", "--method", "POST"});
  }

  @Test
  public void errorDuplicateNonRepeatableShort() {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("Found token: -X2, but option METHOD is not repeatable");
    CurlArguments_Parser.parse(new String[]{"-X1", "-X2"});
  }

  @Test
  public void errorDuplicateNonRepeatableLongDetachedShortAttached() {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("Found token: -X2, but option METHOD is not repeatable");
    CurlArguments_Parser.parse(new String[]{"--method", "1", "-X2"});
  }

  @Test
  public void errorDuplicateNonRepeatableLongAttachedShortDetached() {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("Found token: -X, but option METHOD is not repeatable");
    CurlArguments_Parser.parse(new String[]{"--method=1", "-X", "2"});
  }

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
}
