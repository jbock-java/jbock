package net.jbock.compiler;

import net.jbock.common.EnumName;
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
    assertEquals("f_ancy", EnumName.create("f_ancy").snake('_'));
    assertEquals("f__ancy", EnumName.create("f__ancy").snake('_'));
    assertEquals("__", EnumName.create("__").snake('_'));
  }

  @Test
  void testCamel() {
    assertEquals("MY_ARGUMENTS", EnumName.create("MyArguments").enumConstant());
    assertEquals("GET_MONEY", EnumName.create("getMoney").enumConstant());
    assertEquals("GITHUB", EnumName.create("GIThub").enumConstant());
    assertEquals("MONEY_MAKER", EnumName.create("money_maker").enumConstant());
    assertEquals("ROBO_COP_9", EnumName.create("roboCop9").enumConstant());
    assertEquals("IS_WINDOWSCOMPATIBLE", EnumName.create("isWINDOWScompatible").enumConstant());
    assertEquals("A_REQUIRED_INT", EnumName.create("aRequiredInt").enumConstant());
    assertEquals("THIS_IS_SNAKE", EnumName.create("thisIsSnake").enumConstant());
    assertEquals("FANCY", EnumName.create("fancy").enumConstant());
    assertEquals("F_ANCY", EnumName.create("fAncy").enumConstant());
    assertEquals("F_ANCY", EnumName.create("f_ancy").enumConstant());
    assertEquals("F__ANCY", EnumName.create("f_Ancy").enumConstant());
    assertEquals("F__ANCY", EnumName.create("f__ancy").enumConstant());
    assertEquals("__", EnumName.create("__").enumConstant());
  }
}