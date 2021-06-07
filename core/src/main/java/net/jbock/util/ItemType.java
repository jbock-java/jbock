package net.jbock.util;

/**
 * Item type as determined by its method's annotation.
 * An &quot;Item&quot; is either a <em>named option</em>
 * or a <em>positional parameter</em>.
 *
 * <table>
 *   <thead><tr><td><b>Annotation</b></td><td><b>Item type</b></td></tr></thead>
 *   <tr><td>{@code @Parameter}</td><td>{@link ItemType#PARAMETER PARAMETER}</td></tr>
 *   <tr><td>{@code @Parameters}</td><td>{@link ItemType#PARAMETER PARAMETER}</td></tr>
 *   <tr><td>{@code @Option}</td><td>{@link ItemType#OPTION OPTION}</td></tr>
 * </table>
 */
public enum ItemType {

  /**
   * Named option, or modal flag.
   */
  OPTION,

  /**
   * Positional parameter.
   */
  PARAMETER,
}
