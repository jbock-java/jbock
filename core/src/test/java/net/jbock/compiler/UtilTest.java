package net.jbock.compiler;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static net.jbock.compiler.Util.snakeCase;
import static net.jbock.compiler.Util.snakeToCamel;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class UtilTest {

  @Test
  void testSnakeCase() {
    assertEquals("get_money", snakeCase("getMoney"));
    assertEquals("git_hub", snakeCase("GIThub"));
    assertEquals("money_maker", snakeCase("money_maker"));
    assertEquals("robo_cop_9", snakeCase("roboCop9"));
    assertEquals("is_windows_compatible", snakeCase("isWINDOWScompatible"));
    assertEquals("a_required_int", snakeCase("aRequiredInt"));
  }

  @Test
  void testSnakeToCamel() {
    assertEquals("thisIsSnake", snakeToCamel("this_is_snake"));
    assertEquals("aRequiredInt", snakeToCamel("a_required_int"));
    assertNotEquals(snakeToCamel("f_ancy"), snakeToCamel("f__ancy"));
  }

  @Test
  void testBothWays() {
    for (String s : Arrays.asList(
        "getMoney",
        "gitHub",
        "moneyMaker",
        "roboCop9",
        "isWindowsCompatible",
        "aRequiredInt")) {
      String mapped = snakeToCamel(snakeCase(s));
      assertEquals(s, mapped);
    }
  }
}