package net.zerobuilder.examples.gradle;

import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class RmTest {

  @Test
  public void testRest() {
    RmParser.Binder binder = RmParser.parse(new String[]{"-f", "a", "--", "-r", "--", "-f"});
    Rm rm = binder.bind();
    assertThat(rm.force, is(true));
    assertThat(rm.recursive, is(false));
    assertThat(rm.filesToDelete.size(), is(4));
    assertThat(rm.filesToDelete.get(0), is("a"));
    assertThat(rm.filesToDelete.get(1), is("-r"));
    assertThat(rm.filesToDelete.get(2), is("--"));
    assertThat(rm.filesToDelete.get(3), is("-f"));
  }
}