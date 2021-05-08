package net.jbock.convert;

import java.util.Locale;

public enum Skew {

  REQUIRED, OPTIONAL, REPEATABLE, FLAG;

  public String toString() {
    return name().toLowerCase(Locale.US);
  }
}
