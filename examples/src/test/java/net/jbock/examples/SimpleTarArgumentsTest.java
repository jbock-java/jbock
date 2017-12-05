package net.jbock.examples;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class SimpleTarArgumentsTest {

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  @Test
  public void testExtract() {
    assertThat(SimpleTarArguments_Parser.parse(new String[]{"-x", "-f", "foo.tar"}).file())
        .isEqualTo("foo.tar");
    assertThat(SimpleTarArguments_Parser.parse(new String[]{"-x", "-v", "-f", "foo.tar"}).verbose())
        .isTrue();
    assertThat(SimpleTarArguments_Parser.parse(new String[]{"-x", "-f", "foo.tar"}).file())
        .isEqualTo("foo.tar");
    assertThat(SimpleTarArguments_Parser.parse(new String[]{"-x", "-ffoo.tar"}).file())
        .isEqualTo("foo.tar");
  }

  @Test
  public void missingRequired() throws Exception {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("Missing required option: FILE (-f, --file)");
    assertThat(SimpleTarArguments_Parser.parse(new String[]{"-x"}).extract())
        .isTrue();
  }

  @Test
  public void errorGroupIsNotTheFirstToken() {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("Unknown token: xf");
    SimpleTarArguments_Parser.parse(new String[]{"-v", "xf", "foo.tar"});
  }

  @Test
  public void errorGroupIsNotTheFirstTokenHyphen() {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("Unknown token: -xf");
    SimpleTarArguments_Parser.parse(new String[]{"-v", "-xf", "foo.tar"});
  }
}
