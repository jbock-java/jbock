package net.jbock.compiler;

import org.junit.jupiter.api.Test;

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
    assertEquals("is_windows_compatible", snakeCase("isWINDOWScompatible"));
  }

  @Test
  void testSnakeToCamel() {
    assertEquals("thisIsSnake", snakeToCamel("this_is_snake"));
    assertNotEquals(snakeToCamel("f_ancy"), snakeToCamel("f__ancy"));
  }
}