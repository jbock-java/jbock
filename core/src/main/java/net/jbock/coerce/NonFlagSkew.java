package net.jbock.coerce;

public enum NonFlagSkew {

  REPEATABLE, OPTIONAL, REQUIRED;

  public Skew widen() {
    return Skew.valueOf(name());
  }
}
