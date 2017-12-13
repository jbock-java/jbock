package net.jbock.examples;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import java.util.OptionalInt;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class NoNameArgumentsTest {

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  @Test
  public void basicTest() {
    NoNameArguments noNameArguments = NoNameArguments_Parser.parse(new String[]{"--message=m", "--file=f", "--file=o",
        "--file=o", "--cmos", "-n1"});
    assertThat(noNameArguments.cmos()).isEqualTo(true);
    assertThat(noNameArguments.message()).isEqualTo(Optional.of("m"));
    assertThat(noNameArguments.file().size()).isEqualTo(3);
    assertThat(noNameArguments.file().get(0)).isEqualTo("f");
    assertThat(noNameArguments.file().get(1)).isEqualTo("o");
    assertThat(noNameArguments.file().get(2)).isEqualTo("o");
    assertThat(noNameArguments.number()).isEqualTo(1);
  }

  @Test
  public void testFlag() {
    NoNameArguments noNameArguments = NoNameArguments_Parser.parse(new String[]{"--cmos", "-n1"});
    assertThat(noNameArguments.cmos()).isEqualTo(true);
    assertThat(noNameArguments.number()).isEqualTo(1);
  }

  @Test
  public void testOptionalInt() {
    assertThat(NoNameArguments_Parser.parse(new String[]{"-v", "1", "-n1"}).verbosity())
        .isEqualTo(OptionalInt.of(1));
    assertThat(NoNameArguments_Parser.parse(new String[]{"-v", "1", "-n1"}).number())
        .isEqualTo(1);
    assertThat(NoNameArguments_Parser.parse(new String[]{"-n1"}).verbosity())
        .isEmpty();
    assertThat(NoNameArguments_Parser.parse(new String[]{"-n1"}).number())
        .isEqualTo(1);
  }

  @Test
  public void errorMissingInt() {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("Missing required option: NUMBER (-n, --number)");
    NoNameArguments_Parser.parse(new String[]{"--cmos"});
  }

  @Test
  public void errorUnknownToken() {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("Invalid option: blabla");
    NoNameArguments_Parser.parse(new String[]{"blabla"});
  }
}
