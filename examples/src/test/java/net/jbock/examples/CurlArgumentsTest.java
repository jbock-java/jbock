package net.jbock.examples;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

import net.jbock.examples.fixture.ParserFixture;
import org.junit.Test;

public class CurlArgumentsTest {

  private final ParserFixture<CurlArguments> f =
      ParserFixture.create(CurlArguments_Parser::parse);

  @Test
  public void testEmpty() {
    f.assertThat().parsesTo();
  }

  @Test
  public void testOptional() {
    f.assertThat("--request=")
        .parsesTo("method", "");
    f.assertThat("--request= ")
        .parsesTo("method", " ");
    f.assertThat("--request", "")
        .parsesTo("method", "");
    f.assertThat("-XPUT")
        .parsesTo("method", "PUT");
    f.assertThat("-X", "PUT")
        .parsesTo("method", "PUT");
  }

  @Test
  public void testRepeatable() {
    f.assertThat("-H1")
        .parsesTo("headers", singletonList("1"));
    f.assertThat("-H1", "-H2")
        .parsesTo("headers", asList("1", "2"));
    f.assertThat("-H", "1")
        .parsesTo("headers", singletonList("1"));
    f.assertThat("-H", "1", "-H", "2")
        .parsesTo("headers", asList("1", "2"));
  }

  @Test
  public void variousTests() {
    f.assertThat("-v", "-H1").parsesTo(
        "verbose", true,
        "headers", singletonList("1"));
    f.assertThat("-v", "-i", "-H1").parsesTo(
        "include", true,
        "verbose", true,
        "headers", singletonList("1"));
    f.assertThat("-i", "-v", "-H1").parsesTo(
        "include", true,
        "verbose", true,
        "headers", singletonList("1"));
    f.assertThat("-v", "-i", "1").parsesTo(
        "include", true,
        "verbose", true,
        "urls", singletonList("1"));
    f.assertThat("-v", "-H", "1", "-H2").parsesTo(
        "verbose", true,
        "headers", asList("1", "2"));
    f.assertThat("-v", "-i", "-H", "1", "-H2").parsesTo(
        "include", true,
        "verbose", true,
        "headers", asList("1", "2"));
    f.assertThat("-v", "-H1", "-H2").parsesTo(
        "verbose", true,
        "headers", asList("1", "2"));
    f.assertThat("-v", "-i", "-H1", "-H2").parsesTo(
        "include", true,
        "verbose", true,
        "headers", asList("1", "2"));
    f.assertThat("-v", "-XPOST").parsesTo(
        "verbose", true,
        "method", "POST");
    f.assertThat("-v", "-i", "-XPOST").parsesTo(
        "verbose", true,
        "include", true,
        "method", "POST");
    f.assertThat("-v", "-i", "-XPOST").parsesTo(
        "verbose", true,
        "include", true,
        "method", "POST");
  }

  @Test
  public void errorInvalidGrouping() {
    f.assertThat("-vH1").fails("Invalid option: -vH1");
  }

  @Test
  public void errorInvalidGroupingLong() {
    f.assertThat("-vXPOST").fails("Invalid option: -vXPOST");
  }

  @Test
  public void errorGroupingDuplicateFlag() {
    f.assertThat("-v", "-vH'Content-Type: application/xml'").fails(
        "Invalid option: -vH'Content-Type: application/xml'");
  }

  @Test
  public void errorMissingRepeatable() {
    f.assertThat("-H").fails("Missing value after token: -H");
  }

  @Test
  public void errorMissingNonRepeatable() {
    f.assertThat("--request").fails("Missing value after token: --request");
  }

  @Test
  public void errorDuplicateNonRepeatableLong() {
    f.assertThat("--request", "GET", "--request", "POST").fails(
        "Option METHOD (-X, --request) is not repeatable");
  }

  @Test
  public void errorDuplicateNonRepeatableShort() {
    f.assertThat("-X1", "-X2").fails("Option METHOD (-X, --request) is not repeatable");
  }

  @Test
  public void errorDuplicateNonRepeatableLongDetachedShortAttached() {
    f.assertThat("--request", "1", "-X2").fails(
        "Option METHOD (-X, --request) is not repeatable");
  }

  @Test
  public void errorDuplicateNonRepeatableLongAttachedShortDetached() {
    f.assertThat("--request=1", "-X", "2").fails(
        "Option METHOD (-X, --request) is not repeatable");
  }

  @Test
  public void testPrint() {
    f.assertPrints(
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
