package net.jbock.examples;

import net.jbock.either.Optional;
import net.jbock.examples.fixture.ParserTestFixture;
import org.junit.jupiter.api.Test;

import java.util.List;

class CurlArgumentsTest {

  private final CurlArgumentsParser parser = new CurlArgumentsParser();

  private final ParserTestFixture<CurlArguments> f =
      ParserTestFixture.create(parser::parse);

  @Test
  void testEmpty() {
    f.assertThat(/* empty */) // not an error, no required param
        .has(CurlArguments::method, Optional.empty())
        .has(CurlArguments::headers, List.of())
        .has(CurlArguments::verbose, false)
        .has(CurlArguments::include, false)
        .has(CurlArguments::url, List.of());
  }

  @Test
  void testOptional() {
    f.assertThat("--request=")
        .has(CurlArguments::method, Optional.of(""))
        .has(CurlArguments::headers, List.of())
        .has(CurlArguments::verbose, false)
        .has(CurlArguments::include, false)
        .has(CurlArguments::url, List.of());
    f.assertThat("--request= ")
        .has(CurlArguments::method, Optional.of(" "))
        .has(CurlArguments::headers, List.of())
        .has(CurlArguments::verbose, false)
        .has(CurlArguments::include, false)
        .has(CurlArguments::url, List.of());
    f.assertThat("--request", "")
        .has(CurlArguments::method, Optional.of(""))
        .has(CurlArguments::headers, List.of())
        .has(CurlArguments::verbose, false)
        .has(CurlArguments::include, false)
        .has(CurlArguments::url, List.of());
    f.assertThat("-XPUT")
        .has(CurlArguments::method, Optional.of("PUT"))
        .has(CurlArguments::headers, List.of())
        .has(CurlArguments::verbose, false)
        .has(CurlArguments::include, false)
        .has(CurlArguments::url, List.of());
    f.assertThat("-X", "PUT")
        .has(CurlArguments::method, Optional.of("PUT"))
        .has(CurlArguments::headers, List.of())
        .has(CurlArguments::verbose, false)
        .has(CurlArguments::include, false)
        .has(CurlArguments::url, List.of());
  }

  @Test
  void invalidUnixArgument() {
    f.assertThat("-X=PUT")
        .has(CurlArguments::method, Optional.of("=PUT")) // !
        .has(CurlArguments::headers, List.of())
        .has(CurlArguments::verbose, false)
        .has(CurlArguments::include, false)
        .has(CurlArguments::url, List.of());
  }

  @Test
  void testOneHeader() {
    f.assertThat("-H1")
        .has(CurlArguments::method, Optional.empty())
        .has(CurlArguments::headers, List.of("1"))
        .has(CurlArguments::verbose, false)
        .has(CurlArguments::include, false)
        .has(CurlArguments::url, List.of());
    f.assertThat("-H", "1")
        .has(CurlArguments::method, Optional.empty())
        .has(CurlArguments::headers, List.of("1"))
        .has(CurlArguments::verbose, false)
        .has(CurlArguments::include, false)
        .has(CurlArguments::url, List.of());
  }

  @Test
  void listTwoHeaders() {
    f.assertThat("-H1", "-H2")
        .has(CurlArguments::method, Optional.empty())
        .has(CurlArguments::headers, List.of("1", "2"))
        .has(CurlArguments::verbose, false)
        .has(CurlArguments::include, false)
        .has(CurlArguments::url, List.of());
    f.assertThat("-H", "1", "-H", "2")
        .has(CurlArguments::method, Optional.empty())
        .has(CurlArguments::headers, List.of("1", "2"))
        .has(CurlArguments::verbose, false)
        .has(CurlArguments::include, false)
        .has(CurlArguments::url, List.of());
  }

  @Test
  void variousTests() {
    f.assertThat("-v", "-H1")
        .has(CurlArguments::method, Optional.empty())
        .has(CurlArguments::headers, List.of("1"))
        .has(CurlArguments::verbose, true)
        .has(CurlArguments::include, false)
        .has(CurlArguments::url, List.of());
    f.assertThat("-v", "-i", "-H1")
        .has(CurlArguments::method, Optional.empty())
        .has(CurlArguments::headers, List.of("1"))
        .has(CurlArguments::verbose, true)
        .has(CurlArguments::include, true)
        .has(CurlArguments::url, List.of());
    f.assertThat("-i", "-v", "-H1")
        .has(CurlArguments::method, Optional.empty())
        .has(CurlArguments::headers, List.of("1"))
        .has(CurlArguments::verbose, true)
        .has(CurlArguments::include, true)
        .has(CurlArguments::url, List.of());
    f.assertThat("-v", "-i", "1")
        .has(CurlArguments::method, Optional.empty())
        .has(CurlArguments::headers, List.of())
        .has(CurlArguments::verbose, true)
        .has(CurlArguments::include, true)
        .has(CurlArguments::url, List.of("1"));
    f.assertThat("-v", "-H", "1", "-H2")
        .has(CurlArguments::method, Optional.empty())
        .has(CurlArguments::headers, List.of("1", "2"))
        .has(CurlArguments::verbose, true)
        .has(CurlArguments::include, false)
        .has(CurlArguments::url, List.of());
    f.assertThat("-v", "-i", "-H", "1", "-H2")
        .has(CurlArguments::method, Optional.empty())
        .has(CurlArguments::headers, List.of("1", "2"))
        .has(CurlArguments::verbose, true)
        .has(CurlArguments::include, true)
        .has(CurlArguments::url, List.of());
    f.assertThat("-v", "-H1", "-H2")
        .has(CurlArguments::method, Optional.empty())
        .has(CurlArguments::headers, List.of("1", "2"))
        .has(CurlArguments::verbose, true)
        .has(CurlArguments::include, false)
        .has(CurlArguments::url, List.of());
    f.assertThat("-v", "-i", "-H1", "-H2")
        .has(CurlArguments::method, Optional.empty())
        .has(CurlArguments::headers, List.of("1", "2"))
        .has(CurlArguments::verbose, true)
        .has(CurlArguments::include, true)
        .has(CurlArguments::url, List.of());
    f.assertThat("-v", "-XPOST")
        .has(CurlArguments::method, Optional.of("POST"))
        .has(CurlArguments::headers, List.of())
        .has(CurlArguments::verbose, true)
        .has(CurlArguments::include, false)
        .has(CurlArguments::url, List.of());
    f.assertThat("-v", "-i", "-XPOST")
        .has(CurlArguments::method, Optional.of("POST"))
        .has(CurlArguments::headers, List.of())
        .has(CurlArguments::verbose, true)
        .has(CurlArguments::include, true)
        .has(CurlArguments::url, List.of());
    f.assertThat("-v", "-i", "-XPOST")
        .has(CurlArguments::method, Optional.of("POST"))
        .has(CurlArguments::headers, List.of())
        .has(CurlArguments::verbose, true)
        .has(CurlArguments::include, true)
        .has(CurlArguments::url, List.of());
  }

  @Test
  void testClustering() {
    f.assertThat("-H0", "-vH1", "-H2")
        .has(CurlArguments::method, Optional.empty())
        .has(CurlArguments::headers, List.of("0", "1", "2"))
        .has(CurlArguments::verbose, true)
        .has(CurlArguments::include, false)
        .has(CurlArguments::url, List.of());
    f.assertThat("-vXPOST")
        .has(CurlArguments::method, Optional.of("POST"))
        .has(CurlArguments::headers, List.of())
        .has(CurlArguments::verbose, true)
        .has(CurlArguments::include, false)
        .has(CurlArguments::url, List.of());
  }

  @Test
  void errorClusteringDuplicateFlag() {
    f.assertThat("-v", "-vH'Content-Type: application/xml'")
        .fails("Option '-vH'Content-Type: application/xml'' is a repetition");
  }

  @Test
  void errorMissingRepeatable() {
    f.assertThat("-H")
        .fails("Missing argument after option name: -H");
  }

  @Test
  void errorMissingNonRepeatable() {
    f.assertThat("--request")
        .fails("Missing argument after option name: --request");
  }

  @Test
  void errorDuplicateNonRepeatableLong() {
    f.assertThat("--request", "GET", "--request", "POST")
        .fails("Option '--request' is a repetition");
  }

  @Test
  void errorDuplicateNonRepeatableShort() {
    f.assertThat("-X1", "-X2")
        .fails("Option '-X2' is a repetition");
  }

  @Test
  void errorDuplicateNonRepeatableLongDetachedShortAttached() {
    f.assertThat("--request", "1", "-X2")
        .fails("Option '-X2' is a repetition");
  }

  @Test
  void errorDuplicateNonRepeatableLongAttachedShortDetached() {
    f.assertThat("--request=1", "-X", "2")
        .fails("Option '-X' is a repetition");
  }

  @Test
  void testPrint() {
    f.assertPrintsHelp(
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
