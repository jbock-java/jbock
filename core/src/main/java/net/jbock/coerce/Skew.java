package net.jbock.coerce;

import java.util.Locale;

public enum Skew {

  REPEATABLE, OPTIONAL, REQUIRED, FLAG;

  public String toString() {
    return name().toLowerCase(Locale.US);
  }
}
