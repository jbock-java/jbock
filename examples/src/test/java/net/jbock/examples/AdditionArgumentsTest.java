package net.jbock.examples;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.OptionalInt;
import org.junit.Test;

public class AdditionArgumentsTest {

  @Test
  public void optionalAbsent() {
    assertThat(AdditionArguments_Parser.parse(new String[]{"1", "2"}).a())
        .isEqualTo(1);
    assertThat(AdditionArguments_Parser.parse(new String[]{"1", "2"}).b())
        .isEqualTo(2);
    assertThat(AdditionArguments_Parser.parse(new String[]{"1", "2"}).c())
        .isEmpty();
  }

  @Test
  public void optionalPresent() {
    assertThat(AdditionArguments_Parser.parse(new String[]{"1", "2", "3"}).a())
        .isEqualTo(1);
    assertThat(AdditionArguments_Parser.parse(new String[]{"1", "2", "3"}).b())
        .isEqualTo(2);
    assertThat(AdditionArguments_Parser.parse(new String[]{"1", "2", "3"}).c())
        .isEqualTo(OptionalInt.of(3));
  }

  @Test
  public void testSum() {
    assertThat(AdditionArguments_Parser.parse(new String[]{"1", "-2", "3"}).sum())
        .isEqualTo(2);
    assertThat(AdditionArguments_Parser.parse(new String[]{"-1", "-2", "-3"}).sum())
        .isEqualTo(-6);
    assertThat(AdditionArguments_Parser.parse(new String[]{"-1", "-2", "3"}).sum())
        .isEqualTo(0);
    assertThat(AdditionArguments_Parser.parse(new String[]{"-1", "-2"}).sum())
        .isEqualTo(-3);
  }

  @Test
  public void testPrint() {
    AdditionArguments_Parser.printUsage(System.out, 2);

  }
}
