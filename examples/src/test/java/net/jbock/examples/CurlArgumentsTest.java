package net.jbock.examples;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static net.jbock.examples.fixture.JsonFixture.json;
import static net.jbock.examples.fixture.JsonFixture.readJson;
import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class CurlArgumentsTest {

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  private static JsonNode parse(String... args) {
    return readJson(CurlArguments_Parser.parse(args));
  }

  @Test
  public void testEmpty() {
    assertThat(parse()).isEqualTo(json());
  }

  @Test
  public void testOptional() {
    assertThat(parse()).isEqualTo(json());
    assertThat(parse("--request="))
        .isEqualTo(json("method", ""));
    assertThat(parse("--request= "))
        .isEqualTo(json("method", " "));
    assertThat(parse("--request", ""))
        .isEqualTo(json("method", ""));
    assertThat(parse("-XPUT"))
        .isEqualTo(json("method", "PUT"));
    assertThat(parse("-X", "PUT"))
        .isEqualTo(json("method", "PUT"));
  }

  @Test
  public void testRepeatable() {
    assertThat(parse("-H1"))
        .isEqualTo(json("headers", singletonList("1")));
    assertThat(parse("-H1", "-H2"))
        .isEqualTo(json("headers", asList("1", "2")));
    assertThat(parse("-H", "1"))
        .isEqualTo(json("headers", singletonList("1")));
    assertThat(parse("-H", "1", "-H", "2"))
        .isEqualTo(json("headers", asList("1", "2")));
  }

  @Test
  public void variousTests() {
    assertThat(parse("-v", "-H1"))
        .isEqualTo(json("verbose", true, "headers", singletonList("1")));
    assertThat(parse("-v", "-i", "-H1"))
        .isEqualTo(json("include", true, "verbose", true, "headers", singletonList("1")));
    assertThat(parse("-i", "-v", "-H1"))
        .isEqualTo(json("include", true, "verbose", true, "headers", singletonList("1")));
    assertThat(parse("-v", "-i", "1"))
        .isEqualTo(json("include", true, "verbose", true, "urls", singletonList("1")));
    assertThat(parse("-v", "-H", "1", "-H2"))
        .isEqualTo(json("verbose", true, "headers", asList("1", "2")));
    assertThat(parse("-v", "-i", "-H", "1", "-H2"))
        .isEqualTo(json("include", true, "verbose", true, "headers", asList("1", "2")));
    assertThat(parse("-v", "-H1", "-H2"))
        .isEqualTo(json("verbose", true, "headers", asList("1", "2")));
    assertThat(parse("-v", "-i", "-H1", "-H2"))
        .isEqualTo(json("include", true, "verbose", true, "headers", asList("1", "2")));
    assertThat(parse("-v", "-XPOST"))
        .isEqualTo(json("verbose", true, "method", "POST"));
    assertThat(parse("-v", "-i", "-XPOST"))
        .isEqualTo(json("verbose", true, "include", true, "method", "POST"));
    assertThat(parse("-v", "-i", "-XPOST"))
        .isEqualTo(json("verbose", true, "include", true, "method", "POST"));
  }

  @Test
  public void errorInvalidGrouping() {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("Invalid option: -vH1");
    CurlArguments_Parser.parse(new String[]{"-vH1"});
  }

  @Test
  public void errorInvalidGroupingLong() {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("Invalid option: -vXPOST");
    CurlArguments_Parser.parse(new String[]{"-vXPOST"});
  }

  @Test
  public void errorGroupingDuplicateFlag() {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("Invalid option: -vH'Content-Type: application/xml'");
    CurlArguments_Parser.parse(new String[]{"-v", "-vH'Content-Type: application/xml'"});
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
    exception.expectMessage("Option METHOD (-X, --request) is not repeatable");
    CurlArguments_Parser.parse(new String[]{"--request", "GET", "--request", "POST"});
  }

  @Test
  public void errorDuplicateNonRepeatableShort() {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("Option METHOD (-X, --request) is not repeatable");
    CurlArguments_Parser.parse(new String[]{"-X1", "-X2"});
  }

  @Test
  public void errorDuplicateNonRepeatableLongDetachedShortAttached() {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("Option METHOD (-X, --request) is not repeatable");
    CurlArguments_Parser.parse(new String[]{"--request", "1", "-X2"});
  }

  @Test
  public void errorDuplicateNonRepeatableLongAttachedShortDetached() {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("Option METHOD (-X, --request) is not repeatable");
    CurlArguments_Parser.parse(new String[]{"--request=1", "-X", "2"});
  }

  @Test
  public void testPrintUsage() {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    CurlArguments_Parser.printUsage(new PrintStream(out), 2);
    String[] lines = new String(out.toByteArray()).split("\n", -1);
    assertThat(lines).isEqualTo(new String[]{
        "-X, --request VALUE",
        "  Optional<String> for regular arguments",
        "-H VALUE...",
        "  List<String> for repeatable arguments",
        "-v",
        "  boolean for flags",
        "-i, --include",
        ""});
  }
}
