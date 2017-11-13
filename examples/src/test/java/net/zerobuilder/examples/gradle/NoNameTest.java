package net.zerobuilder.examples.gradle;

import java.util.Optional;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class NoNameTest {

  @Test
  public void test() throws Exception {
    NoName noName = NoName_Parser.parse(new String[]{"--message=m", "--file=f", "--file=o",
        "--file=o", "--cmos"});
    assertThat(noName.cmos(), is(true));
    assertThat(noName.message(), is(Optional.of("m")));
    assertThat(noName.file().size(), is(3));
    assertThat(noName.file().get(0), is("f"));
    assertThat(noName.file().get(1), is("o"));
    assertThat(noName.file().get(2), is("o"));
  }
}
