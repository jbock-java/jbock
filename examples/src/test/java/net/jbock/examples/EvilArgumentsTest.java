package net.jbock.examples;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
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

  @Test
  public void basicOptions() {
    assertThat(EvilArguments_Parser.Option.FANCY_0.longName())
        .isEqualTo(Optional.of("fancy"));
    assertThat(EvilArguments_Parser.Option.F_ANCY_1.longName())
        .isEqualTo(Optional.of("fAncy"));
    assertThat(EvilArguments_Parser.Option.F_ANCY_2.longName())
        .isEqualTo(Optional.of("f_ancy"));
    assertThat(EvilArguments_Parser.Option.BLUB_3.longName())
        .isEqualTo(Optional.of("blub"));
    assertThat(EvilArguments_Parser.Option.BLUB_4.longName())
        .isEqualTo(Optional.of("Blub"));
  }
}
