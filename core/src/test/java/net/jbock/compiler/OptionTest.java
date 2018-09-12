package net.jbock.compiler;

import org.junit.jupiter.api.Test;

import static net.jbock.compiler.Option.cleanDesc;
import static net.jbock.compiler.Option.trim;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class OptionTest {

  @Test
  void testTrim() {
    assertArrayEquals(new String[]{"1"}, trim(new String[]{"1", ""}));
    assertArrayEquals(new String[]{"1", "", "2"}, trim(new String[]{"1", "", "2"}));
    assertArrayEquals(new String[]{"1", "2"}, trim(new String[]{"1", "2"}));
    assertArrayEquals(new String[]{"1", "2"}, trim(new String[]{"", "1", "2", "", ""}));
    assertArrayEquals(new String[]{"1"}, trim(new String[]{"", "1"}));
  }


  @Test
  void testCleanDesc() {
    assertArrayEquals(new String[]{"a", "e"}, cleanDesc(new String[]{"a", "e", "", "@r"}));
  }
}