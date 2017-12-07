package net.jbock.examples;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class SimpleRestArgumentsTest {
  @Test
  public void testRest() {
    assertThat(SimpleRestArguments_Parser.parse(new String[]{"-", "a"}).rest().size())
        .isEqualTo(2);
  }

  @Test
  public void testFiles() {
    assertThat(SimpleRestArguments_Parser.parse(new String[]{"--file=1", "--file", "2", "-", "a"})
        .rest().size())
        .isEqualTo(2);
    assertThat(SimpleRestArguments_Parser.parse(new String[]{"--file=1", "--file", "2", "-", "a"})
        .file().size())
        .isEqualTo(2);
  }

}
