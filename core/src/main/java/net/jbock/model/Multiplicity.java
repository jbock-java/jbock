package net.jbock.model;

import java.util.Locale;

/**
 * Number of times an {@link Item} can appear in the input array.
 */
public enum Multiplicity {

  /**
   * The item is required.
   * It must appear exactly once in the input array.
   */
  REQUIRED,

  /**
   * The item is optional.
   * It must either be absent, or appear exactly once
   * in the input array.
   */
  OPTIONAL,

  /**
   * The item is repeatable.
   * It may appear any number of times in the input array.
   */
  REPEATABLE;

  @Override
  public String toString() {
    return name().toLowerCase(Locale.US);
  }
}
