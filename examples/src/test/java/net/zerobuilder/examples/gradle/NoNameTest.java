package net.zerobuilder.examples.gradle;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import org.junit.Test;

public class NoNameTest {

  @Test
  public void test() throws Exception {
    NoName noName = NoName_Parser.parse(new String[]{"--message=m", "--file=f", "--file=o",
        "--file=o", "--cmos"});
    assertThat(noName.cmos()).isEqualTo(true);
    assertThat(noName.message()).isEqualTo(Optional.of("m"));
    assertThat(noName.file().size()).isEqualTo(3);
    assertThat(noName.file().get(0)).isEqualTo("f");
    assertThat(noName.file().get(1)).isEqualTo("o");
    assertThat(noName.file().get(2)).isEqualTo("o");
  }
}
