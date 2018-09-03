package net.jbock.compiler;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UtilTest {

  @Test
  void snakeCase() {
    assertEquals("get_money", Util.snakeCase("getMoney"));
    assertEquals("git_hub", Util.snakeCase("GIThub"));
    assertEquals("money_maker", Util.snakeCase("money_maker"));
    assertEquals("is_windows_compatible", Util.snakeCase("isWINDOWScompatible"));
  }
}