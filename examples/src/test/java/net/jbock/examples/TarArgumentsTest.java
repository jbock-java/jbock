package net.jbock.examples;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class TarArgumentsTest {

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  @Test
  public void testExtract() {
    assertThat(TarArguments_Parser.parse(new String[]{"-x", "-f", "foo.tar"}).extract())
        .isTrue();
    assertThat(TarArguments_Parser.parse(new String[]{"-x", "-f", "foo.tar"}).file())
        .isEqualTo("foo.tar");
    assertThat(TarArguments_Parser.parse(new String[]{"-vx", "-f", "foo.tar"}).verbose())
        .isTrue();
  }

  @Test
  public void errorGroupIsNotTheFirstToken() {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("Unknown token: xf");
    TarArguments_Parser.parse(new String[]{"-v", "xf", "foo.tar"});
  }

  @Test
  public void errorGroupIsNotTheFirstTokenHyphen() {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("Unknown token: -xf");
    TarArguments_Parser.parse(new String[]{"-v", "-xf", "foo.tar"});
  }
}
