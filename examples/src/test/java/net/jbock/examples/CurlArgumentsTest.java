package net.jbock.examples;

import net.jbock.examples.fixture.ParserTestFixture;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static net.jbock.examples.fixture.ParserTestFixture.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CurlArgumentsTest {

  private final CurlArgumentsParser parser = new CurlArgumentsParser();

  private final ParserTestFixture<CurlArguments> f =
      ParserTestFixture.create(parser);

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
    assertTrue(parser.parse("-v", "-vH'Content-Type: application/xml'")
        .getLeft().map(f::castToError).orElseThrow().message()
        .contains("Option '-vH'Content-Type: application/xml'' is a repetition"));
  }

  @Test
  void errorMissingRepeatable() {
    assertTrue(parser.parse("-H")
        .getLeft().map(f::castToError).orElseThrow().message()
        .contains("Missing argument after token: -H"));
  }

  @Test
  void errorMissingNonRepeatable() {
    assertTrue(parser.parse("--request")
        .getLeft().map(f::castToError).orElseThrow().message()
        .contains("Missing argument after token: --request"));
  }

  @Test
  void errorDuplicateNonRepeatableLong() {
    assertTrue(parser.parse("--request", "GET", "--request", "POST")
        .getLeft().map(f::castToError).orElseThrow().message()
        .contains("Option '--request' is a repetition"));
  }

  @Test
  void errorDuplicateNonRepeatableShort() {
    assertTrue(parser.parse("-X1", "-X2")
        .getLeft().map(f::castToError).orElseThrow().message()
        .contains("Option '-X2' is a repetition"));
  }

  @Test
  void errorDuplicateNonRepeatableLongDetachedShortAttached() {
    assertTrue(parser.parse("--request", "1", "-X2")
        .getLeft().map(f::castToError).orElseThrow().message()
        .contains("Option '-X2' is a repetition"));
  }

  @Test
  void errorDuplicateNonRepeatableLongAttachedShortDetached() {
    assertTrue(parser.parse("--request=1", "-X", "2")
        .getLeft().map(f::castToError).orElseThrow().message()
        .contains("Option '-X' is a repetition"));
  }

  @Test
  void testPrint() {
    String[] actual = parser.parse("--help")
        .getLeft().map(f::getUsageDocumentation).orElseThrow();
    assertEquals(actual,
        "curl is a tool to transfer data from or to a server using one of the supported",
        "protocols. curl offers a busload of useful tricks. curl is powered by libcurl for",
        "all transfer-related features. See libcurl(3) for details.",
        "",
        "\u001B[1mUSAGE\u001B[m",
        "  curl [OPTIONS] URL...",
        "",
        "\u001B[1mPARAMETERS\u001B[m",
        "  URL ",
        "",
        "\u001B[1mOPTIONS\u001B[m",
        "  -X, --request REQUEST  Optional<String> for regular arguments",
        "  -H, --header HEADER    List<String> for repeatable arguments",
        "  -v, --verbose          boolean for flags",
        "  -i, --include         ",
        "");
  }
}
