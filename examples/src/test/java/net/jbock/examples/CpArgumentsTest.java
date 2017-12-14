package net.jbock.examples;

import static net.jbock.examples.fixture.JsonFixture.expectedJson;
import static net.jbock.examples.fixture.JsonFixture.readJson;
import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class CpArgumentsTest {

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  @Test
  public void errorMissingSource() {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("Missing positional parameter: SOURCE");
    CpArguments_Parser.parse(new String[]{});
  }

  @Test
  public void errorMissingDest() {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("Missing positional parameter: DEST");
    CpArguments_Parser.parse(new String[]{"a"});
  }

  @Test
  public void minimal() {
    assertThat(readJson(CpArguments_Parser.parse(new String[]{"a", "b"})))
        .isEqualTo(expectedJson(
            "source", "a",
            "dest", "b"));
    assertThat(readJson(CpArguments_Parser.parse(new String[]{"b", "a"})))
        .isEqualTo(expectedJson(
            "source", "b",
            "dest", "a"));
  }

  @Test
  public void dashNotIgnored() {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("Invalid option: -a");
    CpArguments_Parser.parse(new String[]{"-a", "b"});
  }

  @Test
  public void tooMany() {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("Excess option: c");
    CpArguments_Parser.parse(new String[]{"a", "b", "c"});
  }

  @Test
  public void tooManyAndFlag() {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("Excess option: c");
    CpArguments_Parser.parse(new String[]{"-r", "a", "b", "c"});
  }

  @Test
  public void flag() {
    JsonNode expected = expectedJson(
        "source", "a",
        "dest", "b",
        "recursive", true);
    assertThat(readJson(CpArguments_Parser.parse(new String[]{"-r", "a", "b"})))
        .isEqualTo(expected);
    assertThat(readJson(CpArguments_Parser.parse(new String[]{"a", "-r", "b"})))
        .isEqualTo(expected);
    assertThat(readJson(CpArguments_Parser.parse(new String[]{"a", "b", "-r"})))
        .isEqualTo(expected);
  }
}
