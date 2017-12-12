package net.jbock.examples;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class EvilArgumentsTest {

  @Test
  public void basicTest() {
    EvilArguments args = EvilArguments_Parser.parse(
        new String[]{"--fancy=1", "--fAncy=2", "--f_ancy=3", "--blub=4", "--Blub=5"});
    assertThat(args.fancy()).isEqualTo("1");
    assertThat(args.fAncy()).isEqualTo("2");
    assertThat(args.f_ancy()).isEqualTo("3");
    assertThat(args.blub()).isEqualTo("4");
    assertThat(args.Blub()).isEqualTo("5");
  }
}
