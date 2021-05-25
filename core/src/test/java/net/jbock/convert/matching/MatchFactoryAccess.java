package net.jbock.convert.matching;

import net.jbock.common.EnumName;

public class MatchFactoryAccess {

  public static MatchFactory create(EnumName enumName) {
    return new MatchFactory(enumName);
  }
}