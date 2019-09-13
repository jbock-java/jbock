package net.jbock.compiler;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static net.jbock.compiler.Util.snakeToCamel;
import static org.junit.jupiter.api.Assertions.assertEquals;

class UtilTest {

  @Test
  void testSnakeCase() {
    assertEquals("get_money", ParamName.create("getMoney").snake());
    assertEquals("github", ParamName.create("GIThub").snake());
    assertEquals("money_maker", ParamName.create("money_maker").snake());
    assertEquals("robo_cop_9", ParamName.create("roboCop9").snake());
    assertEquals("is_windowscompatible", ParamName.create("isWINDOWScompatible").snake());
    assertEquals("a_required_int", ParamName.create("aRequiredInt").snake());
  }

  @Test
  void testSnakeToCamel() {
    assertEquals("this_is_snake", ParamName.create("thisIsSnake").snake());
    assertEquals("fancy", ParamName.create("fancy").snake());
    assertEquals("f_ancy", ParamName.create("fAncy").snake());
    assertEquals("f_ancy", ParamName.create("f_ancy").snake());
    assertEquals("f_ancy", ParamName.create("f__ancy").snake());
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
      String mapped = snakeToCamel(ParamName.create(s).snake());
      assertEquals(s, mapped);
    }
  }
}
