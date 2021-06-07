package net.jbock.util;

/**
 * A failure with an error message.
 */
public interface HasMessage {

  /**
   * Returns an error message to describe the failure.
   *
   * @return error message
   */
  String message();
}
