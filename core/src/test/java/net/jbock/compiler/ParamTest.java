package net.jbock.compiler;

import org.junit.jupiter.api.Test;

import static net.jbock.compiler.Parameter.trim;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class ParamTest {

  @Test
  void testTrim() {
    assertArrayEquals(new String[]{"1"}, trim(new String[]{"1", ""}));
    assertArrayEquals(new String[]{"1", "", "2"}, trim(new String[]{"1", "", "2"}));
    assertArrayEquals(new String[]{"1", "2"}, trim(new String[]{"1", "2"}));
    assertArrayEquals(new String[]{"1", "2"}, trim(new String[]{"", "1", "2", "", ""}));
    assertArrayEquals(new String[]{"1"}, trim(new String[]{"", "1"}));
    assertArrayEquals(new String[]{}, trim(new String[]{""}));
  }
}