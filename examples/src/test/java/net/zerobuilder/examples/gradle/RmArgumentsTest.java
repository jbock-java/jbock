package net.zerobuilder.examples.gradle;

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
    assertThat(rm.otherTokens().get(0)).isEqualTo("a");
    assertThat(rm.filesToDelete().size()).isEqualTo(3);
    assertThat(rm.filesToDelete().get(0)).isEqualTo("-r");
    assertThat(rm.filesToDelete().get(1)).isEqualTo("--");
    assertThat(rm.filesToDelete().get(2)).isEqualTo("-f");
  }

  @Test
  public void testPrint() throws Exception {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    RmArguments_Parser.printUsage(new PrintStream(out), 2);
    String[] lines = new String(out.toByteArray()).split("\n", -1);
    assertThat(lines.length).isEqualTo(11);
    assertThat(lines[0]).isEqualTo("-r, --recursive");
    assertThat(lines[1]).isEqualTo("  --- description goes here ---");
    assertThat(lines[2]).isEqualTo("-f, --force");
    assertThat(lines[3]).isEqualTo("  --- description goes here ---");
    assertThat(lines[4]).isEqualTo("Other tokens");
    assertThat(lines[5]).isEqualTo("  --- description goes here ---");
    assertThat(lines[6]).isEqualTo("Everything after '--'");
    assertThat(lines[7]).isEqualTo("  @EverythingAfter to create a last resort");
    assertThat(lines[8]).isEqualTo("  for problematic @OtherTokens.");
    assertThat(lines[9]).isEqualTo("  For example, when the file name is '-f'");
    assertThat(lines[10]).isEqualTo("");
  }
}
