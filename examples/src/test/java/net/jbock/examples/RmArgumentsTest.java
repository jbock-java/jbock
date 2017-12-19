package net.jbock.examples;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import net.jbock.examples.fixture.ParserFixture;
import org.junit.Test;

public class RmArgumentsTest {

  private final ParserFixture<RmArguments> f =
      ParserFixture.create(RmArguments_Parser::parse);

  @Test
  public void testRest() {
    f.assertThat("-f", "a", "--", "-r", "--", "-f").isParsedAs(
        "force", true,
        "otherTokens", singletonList("a"),
        "ddTokens", asList("-r", "--", "-f"));
  }

  @Test
  public void testPrint() {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    RmArguments_Parser.printUsage(new PrintStream(out), 2);
    String[] lines = new String(out.toByteArray()).split("\n", -1);
    assertThat(lines).isEqualTo(new String[]{
        "[OPTION]...",
        "-r, --recursive",
        "-f, --force",
        ""});
  }
}
