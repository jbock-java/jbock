package net.jbock.compiler;

import javax.lang.model.util.Types;

public class TypeTool {

  private static Types TYPES;

  public static Types instance() {
    return TYPES;
  }

  static void setInstance(Types instance) {
    TYPES = instance;
  }

  static void unset() {
    TYPES = null;
  }
}
