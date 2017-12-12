package net.jbock.examples;

import java.util.OptionalInt;
import org.assertj.core.api.Assertions;
import org.junit.Test;

public class AdditionArgumentsTest {

  @Test
  public void optionalAbsent() {
    Assertions.assertThat(AdditionArguments_Parser.parse(new String[]{"1", "2"}).a())
        .isEqualTo(1);
    Assertions.assertThat(AdditionArguments_Parser.parse(new String[]{"1", "2"}).b())
        .isEqualTo(2);
    Assertions.assertThat(AdditionArguments_Parser.parse(new String[]{"1", "2"}).c())
        .isEmpty();
  }

  @Test
  public void optionalPresent() {
    Assertions.assertThat(AdditionArguments_Parser.parse(new String[]{"1", "2", "3"}).a())
        .isEqualTo(1);
    Assertions.assertThat(AdditionArguments_Parser.parse(new String[]{"1", "2", "3"}).b())
        .isEqualTo(2);
    Assertions.assertThat(AdditionArguments_Parser.parse(new String[]{"1", "2", "3"}).c())
        .isEqualTo(OptionalInt.of(3));
  }
}
