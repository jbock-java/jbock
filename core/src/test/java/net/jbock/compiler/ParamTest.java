package net.jbock.compiler;

import org.junit.jupiter.api.Test;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static net.jbock.compiler.Param.cleanDesc;
import static net.jbock.compiler.Param.trim;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

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

  @Test
  void testCleanDesc() {
    assertEquals(asList("a", "e"), cleanDesc(new String[]{"a", "e", "", "@r"}));
    assertEquals(emptyList(), cleanDesc(new String[]{"", "@r"}));
  }
}