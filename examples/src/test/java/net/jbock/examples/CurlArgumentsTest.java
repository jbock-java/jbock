package net.jbock.examples;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

import net.jbock.examples.fixture.ParserTestFixture;
import org.junit.jupiter.api.Test;

class CurlArgumentsTest {

  private ParserTestFixture<CurlArguments> f =
      ParserTestFixture.create(CurlArguments_Parser.create());

  @Test
  void testEmpty() {
    f.assertThat().succeeds(
        "headers", emptyList(),
        "verbose", false,
        "include", false,
        "urls", emptyList(),
        "method", null);
  }

  @Test
  void testOptional() {
    f.assertThat("--request=").succeeds(
        "method", "",
        "include", false,
        "verbose", false,
        "headers", emptyList(),
        "urls", emptyList());
    f.assertThat("--request= ").succeeds(
        "method", " ",
        "include", false,
        "verbose", false,
        "headers", emptyList(),
        "urls", emptyList());
    f.assertThat("--request", "").succeeds(
        "method", "",
        "include", false,
        "verbose", false,
        "headers", emptyList(),
        "urls", emptyList());
    f.assertThat("-XPUT").succeeds(
        "method", "PUT",
        "include", false,
        "verbose", false,
        "headers", emptyList(),
        "urls", emptyList());
    f.assertThat("-X", "PUT").succeeds(
        "method", "PUT",
        "include", false,
        "verbose", false,
        "headers", emptyList(),
        "urls", emptyList());
  }

  @Test
  void testRepeatable() {
    f.assertThat("-H1").succeeds(
        "method", null,
        "include", false,
        "verbose", false,
        "headers", singletonList("1"),
        "urls", emptyList());
    f.assertThat("-H1", "-H2").succeeds(
        "method", null,
        "include", false,
        "verbose", false,
        "headers", asList("1", "2"),
        "urls", emptyList());
    f.assertThat("-H", "1").succeeds(
        "method", null,
        "include", false,
        "verbose", false,
        "headers", singletonList("1"),
        "urls", emptyList());
    f.assertThat("-H", "1", "-H", "2").succeeds(
        "method", null,
        "include", false,
        "verbose", false,
        "headers", asList("1", "2"),
        "urls", emptyList());
  }

  @Test
  void variousTests() {
    f.assertThat("-v", "-H1").succeeds(
        "method", null,
        "include", false,
        "verbose", true,
        "headers", singletonList("1"),
        "urls", emptyList());
    f.assertThat("-v", "-i", "-H1").succeeds(
        "method", null,
        "include", true,
        "verbose", true,
        "headers", singletonList("1"),
        "urls", emptyList());
    f.assertThat("-i", "-v", "-H1").succeeds(
        "method", null,
        "include", true,
        "verbose", true,
        "headers", singletonList("1"),
        "urls", emptyList());
    f.assertThat("-v", "-i", "1").succeeds(
        "method", null,
        "headers", emptyList(),
        "include", true,
        "verbose", true,
        "urls", singletonList("1"));
    f.assertThat("-v", "-H", "1", "-H2").succeeds(
        "method", null,
        "include", false,
        "verbose", true,
        "headers", asList("1", "2"),
        "urls", emptyList());
    f.assertThat("-v", "-i", "-H", "1", "-H2").succeeds(
        "method", null,
        "include", true,
        "verbose", true,
        "headers", asList("1", "2"),
        "urls", emptyList());
    f.assertThat("-v", "-H1", "-H2").succeeds(
        "method", null,
        "include", false,
        "verbose", true,
        "headers", asList("1", "2"),
        "urls", emptyList());
    f.assertThat("-v", "-i", "-H1", "-H2").succeeds(
        "method", null,
        "include", true,
        "verbose", true,
        "headers", asList("1", "2"),
        "urls", emptyList());
    f.assertThat("-v", "-XPOST").succeeds(
        "headers", emptyList(),
        "include", false,
        "verbose", true,
        "method", "POST",
        "urls", emptyList());
    f.assertThat("-v", "-i", "-XPOST").succeeds(
        "headers", emptyList(),
        "verbose", true,
        "include", true,
        "method", "POST",
        "urls", emptyList());
    f.assertThat("-v", "-i", "-XPOST").succeeds(
        "headers", emptyList(),
        "verbose", true,
        "include", true,
        "method", "POST",
        "urls", emptyList());
  }

  @Test
  void errorInvalidGrouping() {
    f.assertThat("-vH1").failsWithLine1("Invalid option: -vH1");
  }

  @Test
  void errorInvalidGroupingLong() {
    f.assertThat("-vXPOST").failsWithLine1("Invalid option: -vXPOST");
  }

  @Test
  void errorGroupingDuplicateFlag() {
    f.assertThat("-v", "-vH'Content-Type: application/xml'").failsWithLine1(
        "Invalid option: -vH'Content-Type: application/xml'");
  }

  @Test
  void errorMissingRepeatable() {
    f.assertThat("-H").failsWithLine1("Missing value after token: -H");
  }

  @Test
  void errorMissingNonRepeatable() {
    f.assertThat("--request").failsWithLine1("Missing value after token: --request");
  }

  @Test
  void errorDuplicateNonRepeatableLong() {
    f.assertThat("--request", "GET", "--request", "POST").failsWithLine1(
        "Option METHOD (-X, --request) is not repeatable");
  }

  @Test
  void errorDuplicateNonRepeatableShort() {
    f.assertThat("-X1", "-X2").failsWithLine1("Option METHOD (-X, --request) is not repeatable");
  }

  @Test
  void errorDuplicateNonRepeatableLongDetachedShortAttached() {
    f.assertThat("--request", "1", "-X2").failsWithLine1(
        "Option METHOD (-X, --request) is not repeatable");
  }

  @Test
  void errorDuplicateNonRepeatableLongAttachedShortDetached() {
    f.assertThat("--request=1", "-X", "2").failsWithLine1(
        "Option METHOD (-X, --request) is not repeatable");
  }

  @Test
  void testPrint() {
    f.assertPrints(
        "NAME",
        "  curl - transfer a URL",
        "",
        "SYNOPSIS",
        "  curl [OPTION]... [URLS]...",
        "",
        "DESCRIPTION",
        "  curl  is  a  tool  to  transfer data from or to a server using one of the supported protocols",
        "",
        "  curl offers a busload of useful tricks",
        "",
        "  curl is powered by libcurl for all transfer-related features. See libcurl(3) for details.",
        "",
        "  -X, --request VALUE",
        "    Optional<String> for regular arguments",
        "",
        "  -H VALUE...",
        "    List<String> for repeatable arguments",
        "",
        "  -v",
        "    boolean for flags",
        "",
        "  -i, --include",
        "",
        "");
  }
}
