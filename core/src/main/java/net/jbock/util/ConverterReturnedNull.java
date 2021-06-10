package net.jbock.util;

/**
 * Indicates that a converter returned {@code null}.
 */
public final class ConverterReturnedNull extends ConverterFailure {

  ConverterReturnedNull() {
  }

  @Override
  public String converterMessage() {
    return "converter returned null";
  }
}
