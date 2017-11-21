package net.zerobuilder.examples.gradle;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class NoNameTest {

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  @Test
  public void test() {
    NoName noName = NoName_Parser.parse(new String[]{"--message=m", "--file=f", "--file=o",
        "--file=o", "--cmos"});
    assertThat(noName.cmos()).isEqualTo(true);
    assertThat(noName.message()).isEqualTo(Optional.of("m"));
    assertThat(noName.file().size()).isEqualTo(3);
    assertThat(noName.file().get(0)).isEqualTo("f");
    assertThat(noName.file().get(1)).isEqualTo("o");
    assertThat(noName.file().get(2)).isEqualTo("o");
  }

  @Test
  public void errorUnknownToken() {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("Unknown token: blabla");
    NoName noName = NoName_Parser.parse(new String[]{"blabla"});
  }
}
