package net.zerobuilder.examples.gradle;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class RmArgumentsTest {

  @Test
  public void testRest() {
    RmArguments rm = RmArguments_Parser.parse(new String[]{"-f", "a", "--", "-r", "--", "-f"});
    assertThat(rm.force()).isEqualTo(true);
    assertThat(rm.recursive()).isEqualTo(false);
    assertThat(rm.otherTokens().size()).isEqualTo(1);
    assertThat(rm.otherTokens().get(0)).isEqualTo("a");
    assertThat(rm.filesToDelete().size()).isEqualTo(3);
    assertThat(rm.filesToDelete().get(0)).isEqualTo("-r");
    assertThat(rm.filesToDelete().get(1)).isEqualTo("--");
    assertThat(rm.filesToDelete().get(2)).isEqualTo("-f");
  }
}
