package net.jbock.examples;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.OptionalInt;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class AdditionArgumentsTest {

  @Rule
  public final ExpectedException exception = ExpectedException.none();

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
  public void wrongNumber() {
    exception.expect(NumberFormatException.class);
    assertThat(AdditionArguments_Parser.parse(new String[]{"-a", "2"}).a())
        .isEqualTo(1);
  }

  @Test
  public void dashesIgnored() {
    assertThat(AdditionArguments_Parser.parse(new String[]{"1", "-2", "3"}).sum())
        .isEqualTo(2);
    assertThat(AdditionArguments_Parser.parse(new String[]{"-1", "-2", "-3"}).sum())
        .isEqualTo(-6);
    assertThat(AdditionArguments_Parser.parse(new String[]{"-1", "-2", "3"}).sum())
        .isEqualTo(0);
    assertThat(AdditionArguments_Parser.parse(new String[]{"-1", "-2"}).sum())
        .isEqualTo(-3);
  }
}
