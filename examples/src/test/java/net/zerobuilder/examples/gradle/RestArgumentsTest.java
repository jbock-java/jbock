package net.zerobuilder.examples.gradle;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class RestArgumentsTest {

  @Test
  public void testRest() {
    assertThat(RestArguments_Parser.parse(new String[]{"-", "a"}).rest().size())
        .isEqualTo(1);
  }

  @Test
  public void testFiles() {
    assertThat(RestArguments_Parser.parse(new String[]{"--file=1", "--file", "2", "-", "a"}).rest().size())
        .isEqualTo(1);
    assertThat(RestArguments_Parser.parse(new String[]{"--file=1", "--file", "2", "-", "a"}).file().size())
        .isEqualTo(2);
  }
}
