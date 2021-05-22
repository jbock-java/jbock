package net.jbock.compiler;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EnumNameTest {

  @Test
  void testSnake() {
    assertEquals("my-arguments", EnumName.create("MyArguments").snake('-'));
    assertEquals("get_money", EnumName.create("getMoney").snake('_'));
    assertEquals("github", EnumName.create("GIThub").snake('_'));
    assertEquals("money_maker", EnumName.create("money_maker").snake('_'));
    assertEquals("robo_cop_9", EnumName.create("roboCop9").snake('_'));
    assertEquals("is_windowscompatible", EnumName.create("isWINDOWScompatible").snake('_'));
    assertEquals("a_required_int", EnumName.create("aRequiredInt").snake('_'));
    assertEquals("this_is_snake", EnumName.create("thisIsSnake").snake('_'));
    assertEquals("fancy", EnumName.create("fancy").snake('_'));
    assertEquals("f_ancy", EnumName.create("fAncy").snake('_'));
    assertEquals("f_ancy", EnumName.create("f_ancy").snake('_')); // collision, see previous line
    assertEquals("f__ancy", EnumName.create("f__ancy").snake('_'));
    assertEquals("__", EnumName.create("__").snake('_'));
  }

  @Test
  void testCamel() {
    assertEquals("myArguments", EnumName.create("MyArguments").enumConstant());
    assertEquals("getMoney", EnumName.create("getMoney").enumConstant());
    assertEquals("github", EnumName.create("GIThub").enumConstant());
    assertEquals("money_maker", EnumName.create("money_maker").enumConstant());
    assertEquals("roboCop9", EnumName.create("roboCop9").enumConstant());
    assertEquals("isWindowscompatible", EnumName.create("isWINDOWScompatible").enumConstant());
    assertEquals("aRequiredInt", EnumName.create("aRequiredInt").enumConstant());
    assertEquals("thisIsSnake", EnumName.create("thisIsSnake").enumConstant());
    assertEquals("fancy", EnumName.create("fancy").enumConstant());
    assertEquals("fAncy", EnumName.create("fAncy").enumConstant());
    assertEquals("f_ancy", EnumName.create("f_ancy").enumConstant()); // no collision
    assertEquals("f__ancy", EnumName.create("f__ancy").enumConstant());
    assertEquals("__", EnumName.create("__").enumConstant());
  }
}