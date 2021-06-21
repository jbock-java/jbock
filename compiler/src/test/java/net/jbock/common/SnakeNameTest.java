package net.jbock.common;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SnakeNameTest {

  @Test
  void testSnake() {
    assertEquals("my-arguments", SnakeName.create("MyArguments").snake('-'));
    assertEquals("get-money", SnakeName.create("getMoney").snake('-'));
    assertEquals("git-hub", SnakeName.create("GIThub").snake('-'));
    assertEquals("money_maker", SnakeName.create("money_maker").snake('-'));
    assertEquals("foo-bar-2000", SnakeName.create("FooBAR2000").snake('-'));
    assertEquals("robo-cop-9", SnakeName.create("roboCop9").snake('-'));
    assertEquals("is-windows-compatible", SnakeName.create("isWINDOWScompatible").snake('-'));
    assertEquals("a-required-int", SnakeName.create("aRequiredInt").snake('-'));
    assertEquals("this-is-snake", SnakeName.create("thisIsSnake").snake('-'));
    assertEquals("fancy", SnakeName.create("fancy").snake('-'));
    assertEquals("f-ancy", SnakeName.create("fAncy").snake('-'));
    assertEquals("f_ancy", SnakeName.create("f_ancy").snake('-'));
    assertEquals("f__ancy", SnakeName.create("f__ancy").snake('-'));
    assertEquals("__", SnakeName.create("__").snake('-'));
  }
}