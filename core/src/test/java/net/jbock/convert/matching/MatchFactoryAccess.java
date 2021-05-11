package net.jbock.convert.matching;

import net.jbock.compiler.EnumName;

public class MatchFactoryAccess {

  public static MatchFactory create(EnumName enumName) {
    return new MatchFactory(enumName);
  }
}