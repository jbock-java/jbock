package net.jbock.examples;

import net.jbock.examples.fixture.JsonFixture;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class CpArgumentsTest {

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  private final JsonFixture f = JsonFixture.create(CpArguments_Parser::parse);

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
    f.assertThat("a", "b").isParsedAs(
        "source", "a",
        "dest", "b");
    f.assertThat("b", "a").isParsedAs(
        "source", "b",
        "dest", "a");
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
  public void flagInVariousPositions() {
    Object[] expected = new Object[]{
        "source", "a",
        "dest", "b",
        "recursive", true};
    f.assertThat("-r", "a", "b")
        .isParsedAs(expected);
    f.assertThat("a", "-r", "b")
        .isParsedAs(expected);
    f.assertThat("a", "b", "-r")
        .isParsedAs(expected);
  }
}
