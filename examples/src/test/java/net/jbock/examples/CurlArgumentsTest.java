package net.jbock.examples;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static net.jbock.examples.fixture.PrintFixture.printFixture;

import net.jbock.examples.fixture.ParserFixture;
import org.junit.Test;

public class CurlArgumentsTest {

  private final ParserFixture<CurlArguments> f =
      ParserFixture.create(CurlArguments_Parser::parse);

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
    f.assertThat("-vH1").isInvalid("Invalid option: -vH1");
  }

  @Test
  public void errorInvalidGroupingLong() {
    f.assertThat("-vXPOST").isInvalid("Invalid option: -vXPOST");
  }

  @Test
  public void errorGroupingDuplicateFlag() {
    f.assertThat("-v", "-vH'Content-Type: application/xml'").isInvalid(
        "Invalid option: -vH'Content-Type: application/xml'");
  }

  @Test
  public void errorMissingRepeatable() {
    f.assertThat("-H").isInvalid("Missing value after token: -H");
  }

  @Test
  public void errorMissingNonRepeatable() {
    f.assertThat("--request").isInvalid("Missing value after token: --request");
  }

  @Test
  public void errorDuplicateNonRepeatableLong() {
    f.assertThat("--request", "GET", "--request", "POST").isInvalid(
        "Option METHOD (-X, --request) is not repeatable");
  }

  @Test
  public void errorDuplicateNonRepeatableShort() {
    f.assertThat("-X1", "-X2").isInvalid("Option METHOD (-X, --request) is not repeatable");
  }

  @Test
  public void errorDuplicateNonRepeatableLongDetachedShortAttached() {
    f.assertThat("--request", "1", "-X2").isInvalid(
        "Option METHOD (-X, --request) is not repeatable");
  }

  @Test
  public void errorDuplicateNonRepeatableLongAttachedShortDetached() {
    f.assertThat("--request=1", "-X", "2").isInvalid(
        "Option METHOD (-X, --request) is not repeatable");
  }

  @Test
  public void testPrint() {
    printFixture(CurlArguments_Parser::printUsage).assertPrints(
        "SYNOPSIS",
        "  [OPTION]... [URLS]...",
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
