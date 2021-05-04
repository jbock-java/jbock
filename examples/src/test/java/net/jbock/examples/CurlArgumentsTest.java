package net.jbock.examples;

import net.jbock.examples.fixture.ParserTestFixture;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

class CurlArgumentsTest {

  private final ParserTestFixture<CurlArguments> f =
      ParserTestFixture.create(new CurlArguments_Parser());

  @Test
  void testEmpty() {
    f.assertThat().succeeds(
        "method", Optional.empty(),
        "headers", emptyList(),
        "verbose", false,
        "include", false,
        "url", emptyList());
  }

  @Test
  void testOptional() {
    f.assertThat("--request=").succeeds(
        "method", Optional.of(""),
        "headers", emptyList(),
        "verbose", false,
        "include", false,
        "url", emptyList());
    f.assertThat("--request= ").succeeds(
        "method", Optional.of(" "),
        "headers", emptyList(),
        "verbose", false,
        "include", false,
        "url", emptyList());
    f.assertThat("--request", "").succeeds(
        "method", Optional.of(""),
        "headers", emptyList(),
        "verbose", false,
        "include", false,
        "url", emptyList());
    f.assertThat("-XPUT").succeeds(
        "method", Optional.of("PUT"),
        "headers", emptyList(),
        "verbose", false,
        "include", false,
        "url", emptyList());
    f.assertThat("-X", "PUT").succeeds(
        "method", Optional.of("PUT"),
        "headers", emptyList(),
        "verbose", false,
        "include", false,
        "url", emptyList());
  }

  @Test
  void testRepeatable() {
    f.assertThat("-H1").succeeds(
        "method", Optional.empty(),
        "headers", singletonList("1"),
        "verbose", false,
        "include", false,
        "url", emptyList());
    f.assertThat("-H1", "-H2").succeeds(
        "method", Optional.empty(),
        "headers", asList("1", "2"),
        "verbose", false,
        "include", false,
        "url", emptyList());
    f.assertThat("-H", "1").succeeds(
        "method", Optional.empty(),
        "headers", singletonList("1"),
        "verbose", false,
        "include", false,
        "url", emptyList());
    f.assertThat("-H", "1", "-H", "2").succeeds(
        "method", Optional.empty(),
        "headers", asList("1", "2"),
        "verbose", false,
        "include", false,
        "url", emptyList());
  }

  @Test
  void variousTests() {
    f.assertThat("-v", "-H1").succeeds(
        "method", Optional.empty(),
        "headers", singletonList("1"),
        "verbose", true,
        "include", false,
        "url", emptyList());
    f.assertThat("-v", "-i", "-H1").succeeds(
        "method", Optional.empty(),
        "headers", singletonList("1"),
        "verbose", true,
        "include", true,
        "url", emptyList());
    f.assertThat("-i", "-v", "-H1").succeeds(
        "method", Optional.empty(),
        "headers", singletonList("1"),
        "verbose", true,
        "include", true,
        "url", emptyList());
    f.assertThat("-v", "-i", "1").succeeds(
        "method", Optional.empty(),
        "headers", emptyList(),
        "verbose", true,
        "include", true,
        "url", singletonList("1"));
    f.assertThat("-v", "-H", "1", "-H2").succeeds(
        "method", Optional.empty(),
        "headers", asList("1", "2"),
        "verbose", true,
        "include", false,
        "url", emptyList());
    f.assertThat("-v", "-i", "-H", "1", "-H2").succeeds(
        "method", Optional.empty(),
        "headers", asList("1", "2"),
        "verbose", true,
        "include", true,
        "url", emptyList());
    f.assertThat("-v", "-H1", "-H2").succeeds(
        "method", Optional.empty(),
        "headers", asList("1", "2"),
        "verbose", true,
        "include", false,
        "url", emptyList());
    f.assertThat("-v", "-i", "-H1", "-H2").succeeds(
        "method", Optional.empty(),
        "headers", asList("1", "2"),
        "verbose", true,
        "include", true,
        "url", emptyList());
    f.assertThat("-v", "-XPOST").succeeds(
        "method", Optional.of("POST"),
        "headers", emptyList(),
        "verbose", true,
        "include", false,
        "url", emptyList());
    f.assertThat("-v", "-i", "-XPOST").succeeds(
        "method", Optional.of("POST"),
        "headers", emptyList(),
        "verbose", true,
        "include", true,
        "url", emptyList());
    f.assertThat("-v", "-i", "-XPOST").succeeds(
        "method", Optional.of("POST"),
        "headers", emptyList(),
        "verbose", true,
        "include", true,
        "url", emptyList());
  }

  @Test
  void testClustering() {
    f.assertThat("-H0", "-vH1", "-H2").succeeds(
        "method", Optional.empty(),
        "headers", asList("0", "1", "2"),
        "verbose", true,
        "include", false,
        "url", emptyList());
    f.assertThat("-vXPOST").succeeds(
        "method", Optional.of("POST"),
        "headers", emptyList(),
        "verbose", true,
        "include", false,
        "url", emptyList());
  }

  @Test
  void errorGroupingDuplicateFlag() {
    f.assertThat("-v", "-vH'Content-Type: application/xml'").failsWithMessage(
        "Option '-vH'Content-Type: application/xml'' is a repetition");
  }

  @Test
  void errorMissingRepeatable() {
    f.assertThat("-H").failsWithMessage("Missing value after token: -H");
  }

  @Test
  void errorMissingNonRepeatable() {
    f.assertThat("--request").failsWithMessage("Missing value after token: --request");
  }

  @Test
  void errorDuplicateNonRepeatableLong() {
    f.assertThat("--request", "GET", "--request", "POST").failsWithMessage(
        "Option '--request' is a repetition");
  }

  @Test
  void errorDuplicateNonRepeatableShort() {
    f.assertThat("-X1", "-X2").failsWithMessage("Option '-X2' is a repetition");
  }

  @Test
  void errorDuplicateNonRepeatableLongDetachedShortAttached() {
    f.assertThat("--request", "1", "-X2").failsWithMessage(
        "Option '-X2' is a repetition");
  }

  @Test
  void errorDuplicateNonRepeatableLongAttachedShortDetached() {
    f.assertThat("--request=1", "-X", "2").failsWithMessage(
        "Option '-X' is a repetition");
  }

  @Test
  void testPrint() {
    f.assertPrintsHelp(
        "curl is a tool to transfer data from or to a server using one of the supported",
        "protocols. curl offers a busload of useful tricks. curl is powered by libcurl for",
        "all transfer-related features. See libcurl(3) for details.",
        "",
        "USAGE",
        "  curl [OPTION]... [URL]...",
        "",
        "PARAMETERS",
        "  URL ",
        "",
        "OPTIONS",
        "  -X, --request REQUEST  Optional<String> for regular arguments",
        "  -H, --header HEADER    List<String> for repeatable arguments",
        "  -v, --verbose          boolean for flags",
        "  -i, --include         ",
        "");
  }
}
