package net.jbock.coerce;

public enum ParameterType {

  REPEATABLE, OPTIONAL, REQUIRED, FLAG;

  public boolean isRepeatable() {
    return this == REPEATABLE;
  }

  public boolean isOptional() {
    return this == OPTIONAL;
  }

  public boolean isRequired() {
    return this == REQUIRED;
  }

  public boolean isFlag() {
    return this == FLAG;
  }
}
