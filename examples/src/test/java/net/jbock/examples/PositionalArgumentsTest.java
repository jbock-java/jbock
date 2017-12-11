package net.jbock.examples;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class PositionalArgumentsTest {

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  @Test
  public void errorMissingSource() {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("Missing positional parameter: SOURCE");
    PositionalArguments_Parser.parse(new String[]{});
  }

  @Test
  public void errorMissingDest() {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("Missing positional parameter: DEST");
    PositionalArguments_Parser.parse(new String[]{"a"});
  }

  @Test
  public void minimal() {
    assertThat(PositionalArguments_Parser.parse(new String[]{"a", "b"}).source())
        .isEqualTo("a");
    assertThat(PositionalArguments_Parser.parse(new String[]{"a", "b"}).dest())
        .isEqualTo("b");
  }

  @Test
  public void otherTokens() {
    assertThat(PositionalArguments_Parser.parse(new String[]{"a", "b", "c", "d"}).otherTokens().size())
        .isEqualTo(1);
    assertThat(PositionalArguments_Parser.parse(new String[]{"a", "b", "c", "d"}).otherTokens().get(0))
        .isEqualTo("d");
  }

  @Test
  public void ddTokens() {
    assertThat(PositionalArguments_Parser.parse(new String[]{"a", "b", "c", "d", "--", "e"}).otherTokens().size())
        .isEqualTo(1);
    assertThat(PositionalArguments_Parser.parse(new String[]{"a", "b", "c", "d", "--", "e"}).otherTokens().get(0))
        .isEqualTo("d");
    assertThat(PositionalArguments_Parser.parse(new String[]{"a", "b", "c", "d", "--", "e"}).ddTokens().size())
        .isEqualTo(1);
    assertThat(PositionalArguments_Parser.parse(new String[]{"a", "b", "c", "d", "--", "e"}).ddTokens().get(0))
        .isEqualTo("e");
    assertThat(PositionalArguments_Parser.parse(new String[]{"a", "b", "c", "--", "e"}).otherTokens())
        .isEmpty();
    assertThat(PositionalArguments_Parser.parse(new String[]{"a", "b", "c", "--", "e"}).ddTokens().size())
        .isEqualTo(1);
    assertThat(PositionalArguments_Parser.parse(new String[]{"a", "b", "c", "--", "e"}).ddTokens().get(0))
        .isEqualTo("e");
    assertThat(PositionalArguments_Parser.parse(new String[]{"a", "b", "c", "--"}).ddTokens())
        .isEmpty();
    assertThat(PositionalArguments_Parser.parse(new String[]{"a", "b", "c"}).ddTokens())
        .isEmpty();
    assertThat(PositionalArguments_Parser.parse(new String[]{"a", "b"}).ddTokens())
        .isEmpty();
  }

}
