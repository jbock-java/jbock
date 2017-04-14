package net.zerobuilder.examples.gradle;

import net.zerobuilder.examples.gradle.GradleManParser.Argument;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public final class GradleManTest {

  private final ExpectedException exception = ExpectedException.none();

  @Test
  public void testCompetingArguments() {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("Competing arguments: --message versus -m");
    GradleManParser.init(new String[]{"-m", "hello", "--message", "goodbye"});
  }

  @Test
  public void testShortMessage() {
    GradleManParser parser = GradleManParser.init(new String[]{"-m", "hello"});
    GradleMan gradleMan = parser.parse();
    assertThat(gradleMan.message, is("hello"));
  }

  @Test
  public void testLongMessage() {
    GradleManParser parser = GradleManParser.init(new String[]{"--message", "hello"});
    GradleMan gradleMan = parser.parse();
    assertThat(gradleMan.message, is("hello"));
  }

  @Test
  public void testShortFile() {
    GradleManParser parser = GradleManParser.init(new String[]{"-f", "file"});
    GradleMan gradleMan = parser.parse();
    assertThat(gradleMan.file, is("file"));
  }

  @Test
  public void testLongFile() {
    GradleManParser parser = GradleManParser.init(new String[]{"--file", "file"});
    GradleMan gradleMan = parser.parse();
    assertThat(gradleMan.file, is(nullValue()));
  }

  @Test
  public void testShortDir() {
    GradleManParser parser = GradleManParser.init(new String[]{"-dir", "dir"});
    GradleMan gradleMan = parser.parse();
    assertThat(gradleMan.dir, is(nullValue()));
  }

  @Test
  public void testLongDir() {
    GradleManParser parser = GradleManParser.init(new String[]{"--dir", "dir"});
    GradleMan gradleMan = parser.parse();
    assertThat(gradleMan.dir, is("dir"));
  }

  @Test
  public void testFlagTrue() {
    GradleManParser parser = GradleManParser.init(new String[]{"-c", "hello"});
    GradleMan gradleMan = parser.parse();
    assertThat(gradleMan.cmos, is("true"));
  }

  @Test
  public void testFlagFalse() {
    GradleManParser parser = GradleManParser.init(new String[]{"-dir", "foo"});
    GradleMan gradleMan = parser.parse();
    assertThat(gradleMan.cmos, is(nullValue()));
  }

  @Test
  public void testPrint() {
    GradleManParser parser = GradleManParser.init(new String[]{"-dir", "foo"});
    parser.summary().stream().map(Argument::describe).forEach(System.out::println);
  }
}
