package net.jbock.examples;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import net.jbock.examples.fixture.JsonFixture;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class CurlArgumentsTest {

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  private final JsonFixture f = JsonFixture.create(CurlArguments_Parser::parse);

  @Test
  public void testEmpty() {
    f.assertThat().isParsedAs();
  }

  @Test
  public void testOptional() {
    f.assertThat("--request=")
        .isParsedAs("method", "");
    f.assertThat("--request= ")
        .isParsedAs("method", " ");
    f.assertThat("--request", "")
        .isParsedAs("method", "");
    f.assertThat("-XPUT")
        .isParsedAs("method", "PUT");
    f.assertThat("-X", "PUT")
        .isParsedAs("method", "PUT");
  }

  @Test
  public void testRepeatable() {
    f.assertThat("-H1")
        .isParsedAs("headers", singletonList("1"));
    f.assertThat("-H1", "-H2")
        .isParsedAs("headers", asList("1", "2"));
    f.assertThat("-H", "1")
        .isParsedAs("headers", singletonList("1"));
    f.assertThat("-H", "1", "-H", "2")
        .isParsedAs("headers", asList("1", "2"));
  }

  @Test
  public void variousTests() {
    f.assertThat("-v", "-H1").isParsedAs(
        "verbose", true,
        "headers", singletonList("1"));
    f.assertThat("-v", "-i", "-H1").isParsedAs(
        "include", true,
        "verbose", true,
        "headers", singletonList("1"));
    f.assertThat("-i", "-v", "-H1").isParsedAs(
        "include", true,
        "verbose", true,
        "headers", singletonList("1"));
    f.assertThat("-v", "-i", "1").isParsedAs(
        "include", true,
        "verbose", true,
        "urls", singletonList("1"));
    f.assertThat("-v", "-H", "1", "-H2").isParsedAs(
        "verbose", true,
        "headers", asList("1", "2"));
    f.assertThat("-v", "-i", "-H", "1", "-H2").isParsedAs(
        "include", true,
        "verbose", true,
        "headers", asList("1", "2"));
    f.assertThat("-v", "-H1", "-H2").isParsedAs(
        "verbose", true,
        "headers", asList("1", "2"));
    f.assertThat("-v", "-i", "-H1", "-H2").isParsedAs(
        "include", true,
        "verbose", true,
        "headers", asList("1", "2"));
    f.assertThat("-v", "-XPOST").isParsedAs(
        "verbose", true,
        "method", "POST");
    f.assertThat("-v", "-i", "-XPOST").isParsedAs(
        "verbose", true,
        "include", true,
        "method", "POST");
    f.assertThat("-v", "-i", "-XPOST").isParsedAs(
        "verbose", true,
        "include", true,
        "method", "POST");
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
    assertThat(lines, is(new String[]{
        "[OPTION]...",
        "-X, --request VALUE",
        "  Optional<String> for regular arguments",
        "-H VALUE...",
        "  List<String> for repeatable arguments",
        "-v",
        "  boolean for flags",
        "-i, --include",
        ""}));
  }
}
