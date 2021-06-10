package net.jbock.model;

/**
 * <p>The number of arguments an {@link Option} takes,
 * per appearance in the input array.</p>
 * <p>Note: A unary option can still be
 * {@link Multiplicity#REPEATABLE REPEATABLE}.
 * This is why higher arities are not explicitly supported.</p>
 */
public enum Arity {

  /**
   * This option does not take an argument.
   * It is, in fact, a mode flag.
   */
  NULLARY,

  /**
   * This option takes a single argument.
   */
  UNARY,
}
