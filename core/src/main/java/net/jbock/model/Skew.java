package net.jbock.model;

import java.util.Locale;

/**
 * An important property of an {@link Item}.
 */
public enum Skew {

  /**
   * The item is required.
   * It must be present in the input array.
   */
  REQUIRED,

  /**
   * The item is optional.
   * It may or may not be present in the input array.
   */
  OPTIONAL,

  /**
   * The item is repeatable.
   */
  REPEATABLE,

  /**
   * The item is a modal flag.
   * A {@link Parameter} can't be a flag,
   * so the item must be an {@link Option}.
   */
  MODAL_FLAG;

  @Override
  public String toString() {
    return name().toLowerCase(Locale.US);
  }
}
