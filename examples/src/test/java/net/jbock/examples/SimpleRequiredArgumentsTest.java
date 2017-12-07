package net.jbock.examples;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class SimpleRequiredArgumentsTest {

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  @Test
  public void success() {
    SimpleRequiredArguments requiredMan = SimpleRequiredArguments_Parser.parse(new String[]{"--dir", "A"});
    assertThat(requiredMan.dir()).isEqualTo("A");
  }

  @Test
  public void errorDirMissing() {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("Missing required option: DIR");
    SimpleRequiredArguments_Parser.parse(new String[]{});
  }

  @Test
  public void errorDetachedDetached() {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("Found token: --dir, but option DIR (--dir) is not repeatable");
    SimpleRequiredArguments_Parser.parse(new String[]{"--dir", "A", "--dir", "B"});
  }

  @Test
  public void errorAttachedDetached() {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("Found token: --dir, but option DIR (--dir) is not repeatable");
    SimpleRequiredArguments_Parser.parse(new String[]{"--dir=A", "--dir", "B"});
  }

  @Test
  public void errorAttachedAttached() {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("Found token: --dir=B, but option DIR (--dir) is not repeatable");
    SimpleRequiredArguments_Parser.parse(new String[]{"--dir=A", "--dir=B"});
  }

  @Test
  public void errorDetachedAttached() {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("Found token: --dir=B, but option DIR (--dir) is not repeatable");
    SimpleRequiredArguments_Parser.parse(new String[]{"--dir", "A", "--dir=B"});
  }

}
