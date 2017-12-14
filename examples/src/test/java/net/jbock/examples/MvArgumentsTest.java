package net.jbock.examples;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class MvArgumentsTest {
  @Rule
  public final ExpectedException exception = ExpectedException.none();

  @Test
  public void errorMissingSource() {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("Missing positional parameter: SOURCE");
    MvArguments_Parser.parse(new String[]{});
  }

  @Test
  public void errorMissingDest() {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("Missing positional parameter: DEST");
    MvArguments_Parser.parse(new String[]{"a"});
  }

  @Test
  public void minimal() {
    assertThat(MvArguments_Parser.parse(new String[]{"a", "b"}).source())
        .isEqualTo("a");
    assertThat(MvArguments_Parser.parse(new String[]{"a", "b"}).dest())
        .isEqualTo("b");
  }

  @Test
  public void dashNotIgnored() {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("Invalid option: -aa");
    MvArguments_Parser.parse(new String[]{"-aa", "b"});
  }

  @Test
  public void tooMany() {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("Excess option: c");
    MvArguments_Parser.parse(new String[]{"a", "b", "c"});
  }
}
