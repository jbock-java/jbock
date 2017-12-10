package net.jbock.examples;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import org.junit.Test;

public class RmArgumentsTest {

  @Test
  public void testRest() {
    RmArguments rm = RmArguments_Parser.parse(new String[]{"-f", "a", "--", "-r", "--", "-f"});
    assertThat(rm.force()).isEqualTo(true);
    assertThat(rm.recursive()).isEqualTo(false);
    assertThat(rm.otherTokens().size()).isEqualTo(1);
    assertThat(rm.ddTokens().size()).isEqualTo(3);
    assertThat(rm.otherTokens().get(0)).isEqualTo("a");
    assertThat(rm.ddTokens().get(0)).isEqualTo("-r");
    assertThat(rm.ddTokens().get(1)).isEqualTo("--");
    assertThat(rm.ddTokens().get(2)).isEqualTo("-f");
  }

  @Test
  public void testPrint() {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    RmArguments_Parser.printUsage(new PrintStream(out), 2);
    String[] lines = new String(out.toByteArray()).split("\n", -1);
    assertThat(lines.length).isEqualTo(9);
    assertThat(lines[0]).isEqualTo("-r, --recursive");
    assertThat(lines[1]).isEqualTo("  --- description goes here ---");
    assertThat(lines[2]).isEqualTo("-f, --force");
    assertThat(lines[3]).isEqualTo("  --- description goes here ---");
    assertThat(lines[4]).isEqualTo("(positional arguments)");
    assertThat(lines[5]).isEqualTo("  --- description goes here ---");
    assertThat(lines[6]).isEqualTo("(positional arguments)");
    assertThat(lines[7]).isEqualTo("  --- description goes here ---");
    assertThat(lines[8]).isEqualTo("");
  }
}
