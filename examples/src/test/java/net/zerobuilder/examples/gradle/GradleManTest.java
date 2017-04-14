package net.zerobuilder.examples.gradle;

import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class GradleManTest {

  @Test
  public void test() {
    GradleManParser parser = GradleManParser.init(new String[]{"-message", "hello"});
    GradleMan gradleMan = parser.parse();
    System.out.println(parser.longNames);
    System.out.println(parser.shortNames);
    assertThat(gradleMan.message, is("hello"));
  }
}
