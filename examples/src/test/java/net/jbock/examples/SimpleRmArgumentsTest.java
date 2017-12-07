package net.jbock.examples;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class SimpleRmArgumentsTest {

  @Test
  public void testRest() {
    SimpleRmArguments rm = SimpleRmArguments_Parser.parse(new String[]{"-f", "a", "--", "-r", "--", "-f"});
    assertThat(rm.force()).isEqualTo(true);
    assertThat(rm.recursive()).isEqualTo(false);
    assertThat(rm.otherTokens().size()).isEqualTo(5);
    assertThat(rm.otherTokens().get(0)).isEqualTo("a");
    assertThat(rm.otherTokens().get(1)).isEqualTo("--");
    assertThat(rm.otherTokens().get(2)).isEqualTo("-r");
    assertThat(rm.otherTokens().get(3)).isEqualTo("--");
    assertThat(rm.otherTokens().get(4)).isEqualTo("-f");
  }
}
