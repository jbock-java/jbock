package net.zerobuilder.examples.gradle;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class RmArgumentsTest {

  @Test
  public void testRest() {
    RmArguments rm = RmArguments_Parser.parse(new String[]{"-f", "a", "--", "-r", "--", "-f"});
    assertThat(rm.force(), is(true));
    assertThat(rm.recursive(), is(false));
    assertThat(rm.otherTokens().size(), is(1));
    assertThat(rm.otherTokens().get(0), is("a"));
    assertThat(rm.filesToDelete().size(), is(3));
    assertThat(rm.filesToDelete().get(0), is("-r"));
    assertThat(rm.filesToDelete().get(1), is("--"));
    assertThat(rm.filesToDelete().get(2), is("-f"));
  }
}
