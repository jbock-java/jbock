package net.jbock.examples;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import java.util.OptionalInt;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class NoNameTest {

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  @Test
  public void basicTest() {
    NoName noName = NoName_Parser.parse(new String[]{"--message=m", "--file=f", "--file=o",
        "--file=o", "--cmos", "-n1"});
    assertThat(noName.cmos()).isEqualTo(true);
    assertThat(noName.message()).isEqualTo(Optional.of("m"));
    assertThat(noName.file().size()).isEqualTo(3);
    assertThat(noName.file().get(0)).isEqualTo("f");
    assertThat(noName.file().get(1)).isEqualTo("o");
    assertThat(noName.file().get(2)).isEqualTo("o");
    assertThat(noName.number()).isEqualTo(1);
  }

  @Test
  public void testFlag() {
    NoName noName = NoName_Parser.parse(new String[]{"--cmos", "-n1"});
    assertThat(noName.cmos()).isEqualTo(true);
    assertThat(noName.number()).isEqualTo(1);
  }

  @Test
  public void testOptionalInt() {
    assertThat(NoName_Parser.parse(new String[]{"-v", "1", "-n1"}).verbosity())
        .isEqualTo(OptionalInt.of(1));
    assertThat(NoName_Parser.parse(new String[]{"-v", "1", "-n1"}).number())
        .isEqualTo(1);
    assertThat(NoName_Parser.parse(new String[]{"-n1"}).verbosity())
        .isEmpty();
    assertThat(NoName_Parser.parse(new String[]{"-n1"}).number())
        .isEqualTo(1);
  }

  @Test
  public void errorMissingInt() {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("Missing required option: NUMBER (-n, --number)");
    NoName_Parser.parse(new String[]{"--cmos"});
  }

  @Test
  public void errorUnknownToken() {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("Unknown token: blabla");
    NoName_Parser.parse(new String[]{"blabla"});
  }
}
