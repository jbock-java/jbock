package net.jbock.examples;

import net.jbock.examples.fixture.ParserTestFixture;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

class CurlArgumentsTest {

  private ParserTestFixture<CurlArguments> f =
      ParserTestFixture.create(CurlArguments_Parser.create());

  @Test
  void testEmpty() {
    f.assertThat().succeeds(
        "method", Optional.empty(),
        "headers", emptyList(),
        "verbose", false,
        "include", false,
        "urls", emptyList());
  }

  @Test
  void testOptional() {
    f.assertThat("--request=").succeeds(
        "method", Optional.of(""),
        "headers", emptyList(),
        "verbose", false,
        "include", false,
        "urls", emptyList());
    f.assertThat("--request= ").succeeds(
        "method", Optional.of(" "),
        "headers", emptyList(),
        "verbose", false,
        "include", false,
        "urls", emptyList());
    f.assertThat("--request", "").succeeds(
        "method", Optional.of(""),
        "headers", emptyList(),
        "verbose", false,
        "include", false,
        "urls", emptyList());
    f.assertThat("-XPUT").succeeds(
        "method", Optional.of("PUT"),
        "headers", emptyList(),
        "verbose", false,
        "include", false,
        "urls", emptyList());
    f.assertThat("-X", "PUT").succeeds(
        "method", Optional.of("PUT"),
        "headers", emptyList(),
        "verbose", false,
        "include", false,
        "urls", emptyList());
  }

  @Test
  void testRepeatable() {
    f.assertThat("-H1").succeeds(
        "method", Optional.empty(),
        "headers", singletonList("1"),
        "verbose", false,
        "include", false,
        "urls", emptyList());
    f.assertThat("-H1", "-H2").succeeds(
        "method", Optional.empty(),
        "headers", asList("1", "2"),
        "verbose", false,
        "include", false,
        "urls", emptyList());
    f.assertThat("-H", "1").succeeds(
        "method", Optional.empty(),
        "headers", singletonList("1"),
        "verbose", false,
        "include", false,
        "urls", emptyList());
    f.assertThat("-H", "1", "-H", "2").succeeds(
        "method", Optional.empty(),
        "headers", asList("1", "2"),
        "verbose", false,
        "include", false,
        "urls", emptyList());
  }

  @Test
  void variousTests() {
    f.assertThat("-v", "-H1").succeeds(
        "method", Optional.empty(),
        "headers", singletonList("1"),
        "verbose", true,
        "include", false,
        "urls", emptyList());
    f.assertThat("-v", "-i", "-H1").succeeds(
        "method", Optional.empty(),
        "headers", singletonList("1"),
        "verbose", true,
        "include", true,
        "urls", emptyList());
    f.assertThat("-i", "-v", "-H1").succeeds(
        "method", Optional.empty(),
        "headers", singletonList("1"),
        "verbose", true,
        "include", true,
        "urls", emptyList());
    f.assertThat("-v", "-i", "1").succeeds(
        "method", Optional.empty(),
        "headers", emptyList(),
        "verbose", true,
        "include", true,
        "urls", singletonList("1"));
    f.assertThat("-v", "-H", "1", "-H2").succeeds(
        "method", Optional.empty(),
        "headers", asList("1", "2"),
        "verbose", true,
        "include", false,
        "urls", emptyList());
    f.assertThat("-v", "-i", "-H", "1", "-H2").succeeds(
        "method", Optional.empty(),
        "headers", asList("1", "2"),
        "verbose", true,
        "include", true,
        "urls", emptyList());
    f.assertThat("-v", "-H1", "-H2").succeeds(
        "method", Optional.empty(),
        "headers", asList("1", "2"),
        "verbose", true,
        "include", false,
        "urls", emptyList());
    f.assertThat("-v", "-i", "-H1", "-H2").succeeds(
        "method", Optional.empty(),
        "headers", asList("1", "2"),
        "verbose", true,
        "include", true,
        "urls", emptyList());
    f.assertThat("-v", "-XPOST").succeeds(
        "method", Optional.of("POST"),
        "headers", emptyList(),
        "verbose", true,
        "include", false,
        "urls", emptyList());
    f.assertThat("-v", "-i", "-XPOST").succeeds(
        "method", Optional.of("POST"),
        "headers", emptyList(),
        "verbose", true,
        "include", true,
        "urls", emptyList());
    f.assertThat("-v", "-i", "-XPOST").succeeds(
        "method", Optional.of("POST"),
        "headers", emptyList(),
        "verbose", true,
        "include", true,
        "urls", emptyList());
  }

  @Test
  void errorInvalidGrouping() {
    f.assertThat("-vH1").failsWithUsageMessage("Invalid option: -vH1");
  }

  @Test
  void errorInvalidGroupingLong() {
    f.assertThat("-vXPOST").failsWithUsageMessage("Invalid option: -vXPOST");
  }

  @Test
  void errorGroupingDuplicateFlag() {
    f.assertThat("-v", "-vH'Content-Type: application/xml'").failsWithUsageMessage(
        "Invalid option: -vH'Content-Type: application/xml'");
  }

  @Test
  void errorMissingRepeatable() {
    f.assertThat("-H").failsWithUsageMessage("Missing value after token: -H");
  }

  @Test
  void errorMissingNonRepeatable() {
    f.assertThat("--request").failsWithUsageMessage("Missing value after token: --request");
  }

  @Test
  void errorDuplicateNonRepeatableLong() {
    f.assertThat("--request", "GET", "--request", "POST").failsWithUsageMessage(
        "Option METHOD (-X, --request) is not repeatable");
  }

  @Test
  void errorDuplicateNonRepeatableShort() {
    f.assertThat("-X1", "-X2").failsWithUsageMessage("Option METHOD (-X, --request) is not repeatable");
  }

  @Test
  void errorDuplicateNonRepeatableLongDetachedShortAttached() {
    f.assertThat("--request", "1", "-X2").failsWithUsageMessage(
        "Option METHOD (-X, --request) is not repeatable");
  }

  @Test
  void errorDuplicateNonRepeatableLongAttachedShortDetached() {
    f.assertThat("--request=1", "-X", "2").failsWithUsageMessage(
        "Option METHOD (-X, --request) is not repeatable");
  }

  @Test
  void testPrint() {
    f.assertPrintsHelp(
        "NAME",
        "  curl - transfer a URL",
        "",
        "SYNOPSIS",
        "  curl [OPTIONS...] [<urls>...]",
        "",
        "DESCRIPTION",
        "  curl  is  a  tool  to  transfer data from or to a server",
        "  using one of the supported protocols.",
        "",
        "  curl offers a busload of useful tricks.",
        "",
        "  curl is powered by libcurl for all transfer-related features.",
        "  See libcurl(3) for details.",
        "",
        "URLS",
        "",
        "OPTIONS",
        "  -X <method>, --request <method>",
        "    Optional<String> for regular arguments",
        "",
        "  -H <headers...>",
        "    List<String> for repeatable arguments",
        "",
        "  -v, --verbose",
        "    boolean for flags",
        "",
        "  -i, --include",
        "",
        "  --help",
        "    Print this help page.",
        "    The help flag may only be passed as the first argument.",
        "    Any further arguments will be ignored.",
        "",
        "");
  }
}
