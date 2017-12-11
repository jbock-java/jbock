package net.jbock.examples;

import static org.assertj.core.api.Assertions.assertThat;

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
    assertThat(CpArguments_Parser.parse(new String[]{"a", "b"}).source())
        .isEqualTo("a");
    assertThat(CpArguments_Parser.parse(new String[]{"a", "b"}).dest())
        .isEqualTo("b");
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
}
