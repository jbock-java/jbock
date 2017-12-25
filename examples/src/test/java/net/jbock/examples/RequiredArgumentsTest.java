package net.jbock.examples;

import net.jbock.examples.fixture.ParserFixture;
import org.junit.Test;

public class RequiredArgumentsTest {

  private final ParserFixture<RequiredArguments> f =
      ParserFixture.create(RequiredArguments_Parser::parse);

  @Test
  public void success() {
    f.assertThat("--dir", "A").parsesTo("dir", "A");
  }

  @Test
  public void errorDirMissing() {
    f.assertThat().failsWithLine1("Missing required option: DIR (--dir)");
  }

  @Test
  public void errorRepeatedArgument() {
    f.assertThat("--dir", "A", "--dir", "B").failsWithLine1(
        "Option DIR (--dir) is not repeatable");
    f.assertThat("--dir=A", "--dir", "B").failsWithLine1(
        "Option DIR (--dir) is not repeatable");
    f.assertThat("--dir=A", "--dir=B").failsWithLine1(
        "Option DIR (--dir) is not repeatable");
    f.assertThat("--dir", "A", "--dir=B").failsWithLine1(
        "Option DIR (--dir) is not repeatable");
  }

  @Test
  public void errorDetachedAttached() {
    f.assertThat("--dir", "A", "--dir=B").failsWithLine1("Option DIR (--dir) is not repeatable");
  }

  @Test
  public void testPrint() {
    f.assertPrints(
        "NAME",
        "  RequiredArguments",
        "",
        "SYNOPSIS",
        "  RequiredArguments --dir=DIR [OTHER_TOKENS]...",
        "",
        "DESCRIPTION",
        "",
        "  --dir VALUE",
        "",
        "");
  }
}
