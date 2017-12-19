package net.jbock.examples;

import net.jbock.examples.fixture.ParserFixture;
import org.junit.Test;

public class RequiredArgumentsTest {

  private final ParserFixture<RequiredArguments> f =
      ParserFixture.create(RequiredArguments_Parser::parse);

  @Test
  public void success() {
    f.assertThat("--dir", "A").isParsedAs("dir", "A");
  }

  @Test
  public void errorDirMissing() {
    f.assertThat().isInvalid("Missing required option: DIR");
  }

  @Test
  public void errorRepeatedArgument() {
    f.assertThat("--dir", "A", "--dir", "B").isInvalid(
        "Option DIR (--dir) is not repeatable");
    f.assertThat("--dir=A", "--dir", "B").isInvalid(
        "Option DIR (--dir) is not repeatable");
    f.assertThat("--dir=A", "--dir=B").isInvalid(
        "Option DIR (--dir) is not repeatable");
    f.assertThat("--dir", "A", "--dir=B").isInvalid(
        "Option DIR (--dir) is not repeatable");
  }

  @Test
  public void errorDetachedAttached() {
    f.assertThat("--dir", "A", "--dir=B").isInvalid("Option DIR (--dir) is not repeatable");
  }
}
