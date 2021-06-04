package net.jbock.util;

/**
 * Indicates that a converter returned {@code null}.
 */
public final class ConverterReturnedNull implements ConverterFailure {

  ConverterReturnedNull() {
  }

  @Override
  public String message() {
    return "converter returned null";
  }
}
