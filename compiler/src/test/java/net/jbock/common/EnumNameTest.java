package net.jbock.common;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EnumNameTest {

  @Test
  void testMakeLonger() {
    assertEquals("X_1", EnumName.create("x").makeLonger().enumConstant());
    assertEquals("X11", EnumName.create("x1").makeLonger().enumConstant());
  }

  @Test
  void testEnumConstantUpperCase() {
    assertEquals("MYCOMMAND", EnumName.create("MyCommand").enumConstant());
  }
}