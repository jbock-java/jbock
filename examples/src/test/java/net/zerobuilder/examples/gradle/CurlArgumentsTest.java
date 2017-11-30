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
    assertThat(CurlArguments_Parser.parse(new String[]{"--request="}).method())
        .isEqualTo(Optional.of(""));
    assertThat(CurlArguments_Parser.parse(new String[]{"--request= "}).method())
        .isEqualTo(Optional.of(" "));
    assertThat(CurlArguments_Parser.parse(new String[]{"--request", ""}).method())
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
    assertThat(CurlArguments_Parser.parse(new String[]{"-H1"}).headers())
        .isEqualTo(singletonList("1"));
    assertThat(CurlArguments_Parser.parse(new String[]{"-H1", "-H2"}).headers())
        .isEqualTo(asList("1", "2"));
    assertThat(CurlArguments_Parser.parse(new String[]{"-H", "1"}).headers())
        .isEqualTo(singletonList("1"));
    assertThat(CurlArguments_Parser.parse(new String[]{"-H", "1", "-H", "2"}).headers())
        .isEqualTo(asList("1", "2"));
  }

  @Test
  public void testGrouping() {
    assertThat(CurlArguments_Parser.parse(new String[]{"-v", "-H1"})
        .headers()).isEqualTo(singletonList("1"));
    assertThat(CurlArguments_Parser.parse(new String[]{"-v", "-H1"})
        .verbose()).isTrue();
    assertThat(CurlArguments_Parser.parse(new String[]{"v", "-H1"})
        .verbose()).isTrue();
    assertThat(CurlArguments_Parser.parse(new String[]{"vi", "-H1"})
        .verbose()).isTrue();
    assertThat(CurlArguments_Parser.parse(new String[]{"vi", "-H1"})
        .headers()).isEqualTo(singletonList("1"));
    assertThat(CurlArguments_Parser.parse(new String[]{"-vi", "-H1"})
        .verbose()).isTrue();
    assertThat(CurlArguments_Parser.parse(new String[]{"-vi", "-H1"})
        .headers()).isEqualTo(singletonList("1"));
    assertThat(CurlArguments_Parser.parse(new String[]{"-v", "-H1"})
        .include()).isFalse();
    assertThat(CurlArguments_Parser.parse(new String[]{"-vi", "-H1"})
        .include()).isTrue();
    assertThat(CurlArguments_Parser.parse(new String[]{"vi", "-H1"})
        .include()).isTrue();
    assertThat(CurlArguments_Parser.parse(new String[]{"iv", "-H1"})
        .include()).isTrue();
    assertThat(CurlArguments_Parser.parse(new String[]{"-v", "-H1"})
        .verbose()).isTrue();
    assertThat(CurlArguments_Parser.parse(new String[]{"-vi", "1"})
        .verbose()).isTrue();
    assertThat(CurlArguments_Parser.parse(new String[]{"-v", "-H1"})
        .include()).isFalse();
    assertThat(CurlArguments_Parser.parse(new String[]{"-vi", "1"})
        .include()).isTrue();
    assertThat(CurlArguments_Parser.parse(new String[]{"-v", "-H", "1", "-H2"})
        .headers()).isEqualTo(asList("1", "2"));
    assertThat(CurlArguments_Parser.parse(new String[]{"-vi", "-H", "1", "-H2"})
        .headers()).isEqualTo(asList("1", "2"));
    assertThat(CurlArguments_Parser.parse(new String[]{"-v", "-H", "1", "-H2"})
        .include()).isFalse();
    assertThat(CurlArguments_Parser.parse(new String[]{"-vi", "-H", "1", "-H2"})
        .include()).isTrue();
    assertThat(CurlArguments_Parser.parse(new String[]{"-v", "-H1", "-H2"})
        .verbose()).isTrue();
    assertThat(CurlArguments_Parser.parse(new String[]{"-vi", "-H1", "-H2"})
        .verbose()).isTrue();
    assertThat(CurlArguments_Parser.parse(new String[]{"-v", "-H1", "-H2"})
        .include()).isFalse();
    assertThat(CurlArguments_Parser.parse(new String[]{"-vi", "-H1", "-H2"})
        .include()).isTrue();
    assertThat(CurlArguments_Parser.parse(new String[]{"-v", "-XPOST"})
        .method()).isEqualTo(Optional.of("POST"));
    assertThat(CurlArguments_Parser.parse(new String[]{"-vi", "-XPOST"})
        .verbose()).isTrue();
    assertThat(CurlArguments_Parser.parse(new String[]{"-v", "-XPOST"})
        .include()).isFalse();
    assertThat(CurlArguments_Parser.parse(new String[]{"-vi", "-XPOST"})
        .include()).isTrue();
  }

  @Test
  public void errorInvalidGrouping() {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("Invalid token in option group '-vH1': 'H'");
    CurlArguments_Parser.parse(new String[]{"-vH1"});
  }

  @Test
  public void errorInvalidGroupingLong() {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("Invalid token in option group '-vXPOST': 'X'");
    CurlArguments_Parser.parse(new String[]{"-vXPOST"});
  }

  @Test
  public void errorGroupingDuplicateFlag() {
    assertThat(CurlArguments_Parser.parse(new String[]{"-v", "-vH'Content-Type: application/xml'"}).urls().size())
        .isEqualTo(1);
    assertThat(CurlArguments_Parser.parse(new String[]{"-v", "-vH'Content-Type: application/xml'"}).urls().get(0))
        .isEqualTo("-vH'Content-Type: application/xml'");
  }

  @Test
  public void errorMissingRepeatable() {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("Missing value after token: -H");
    CurlArguments_Parser.parse(new String[]{"-H"});
  }

  @Test
  public void errorMissingNonRepeatable() {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("Missing value after token: --request");
    CurlArguments_Parser.parse(new String[]{"--request"});
  }

  @Test
  public void errorDuplicateNonRepeatableLong() {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("Found token: --request, but option METHOD (-X, --request) is not repeatable");
    CurlArguments_Parser.parse(new String[]{"--request", "GET", "--request", "POST"});
  }

  @Test
  public void errorDuplicateNonRepeatableShort() {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("Found token: -X2, but option METHOD (-X, --request) is not repeatable");
    CurlArguments_Parser.parse(new String[]{"-X1", "-X2"});
  }

  @Test
  public void errorDuplicateNonRepeatableLongDetachedShortAttached() {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("Found token: -X2, but option METHOD (-X, --request) is not repeatable");
    CurlArguments_Parser.parse(new String[]{"--request", "1", "-X2"});
  }

  @Test
  public void errorDuplicateNonRepeatableLongAttachedShortDetached() {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("Found token: -X, but option METHOD (-X, --request) is not repeatable");
    CurlArguments_Parser.parse(new String[]{"--request=1", "-X", "2"});
  }

  @Test
  public void testPrintUsage() {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    CurlArguments_Parser.printUsage(new PrintStream(out), 2);
    String[] lines = new String(out.toByteArray()).split("\n", -1);
    assertThat(lines.length).isEqualTo(18);
    assertThat(lines[0]).isEqualTo("-X, --request VAL");
    assertThat(lines[1]).isEqualTo("  Optional<String> for regular arguments");
    assertThat(lines[2]).isEqualTo("-H VAL");
    assertThat(lines[3]).isEqualTo("  List<String> for repeatable arguments");
    assertThat(lines[4]).isEqualTo("-v");
    assertThat(lines[5]).isEqualTo("  boolean for flags");
    assertThat(lines[6]).isEqualTo("-i, --include");
    assertThat(lines[7]).isEqualTo("  --- description goes here ---");
    assertThat(lines[8]).isEqualTo("Other tokens");
    assertThat(lines[9]).isEqualTo("  @OtherTokens to capture any 'other' tokens in the input.");
    assertThat(lines[10]).isEqualTo("  In this case, that's any token which doesn't match one of");
    assertThat(lines[11]).isEqualTo("  /-v/, /-X(=.*)?/, /--request(=.*)?/, or /-H(=.*)?/,");
    assertThat(lines[12]).isEqualTo("  or follows immediately after the equality-less version");
    assertThat(lines[13]).isEqualTo("  of one of the latter 3.");
    assertThat(lines[14]).isEqualTo("  If there were no method with the @OtherTokens annotation,");
    assertThat(lines[15]).isEqualTo("  such a token would cause an IllegalArgumentException to be");
    assertThat(lines[16]).isEqualTo("  thrown from the CurlArguments_Parser.parse method.");
    assertThat(lines[17]).isEqualTo("");
  }
}
