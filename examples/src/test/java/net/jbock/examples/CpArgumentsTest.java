package net.jbock.examples;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class CpArgumentsTest {

  @Test
  public void source() {
    assertThat(CpArguments_Parser.parse(new String[]{"a", "b"}).source())
        .isEqualTo("a");
    assertThat(CpArguments_Parser.parse(new String[]{"a", "b"}).dest())
        .isEqualTo("b");
    assertThat(CpArguments_Parser.parse(new String[]{"a", "b", "c"}).otherTokens().size())
        .isEqualTo(1);
    assertThat(CpArguments_Parser.parse(new String[]{"a", "b", "c"}).otherTokens().get(0))
        .isEqualTo("c");
    assertThat(CpArguments_Parser.parse(new String[]{"a", "b", "c", "--", "d"}).otherTokens().size())
        .isEqualTo(1);
    assertThat(CpArguments_Parser.parse(new String[]{"a", "b", "c", "--", "d"}).otherTokens().get(0))
        .isEqualTo("c");
    assertThat(CpArguments_Parser.parse(new String[]{"a", "b", "c", "--", "d"}).ddTokens().size())
        .isEqualTo(1);
    assertThat(CpArguments_Parser.parse(new String[]{"a", "b", "c", "--", "d"}).ddTokens().get(0))
        .isEqualTo("d");
  }
}
