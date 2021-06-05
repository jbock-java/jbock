package net.jbock.model;

import java.util.Locale;

public enum Skew {

  REQUIRED, OPTIONAL, REPEATABLE, MODAL_FLAG;

  @Override
  public String toString() {
    return name().toLowerCase(Locale.US);
  }
}
