package net.jbock.util;

/**
 * Item type as determined by its annotation.
 *
 * <table>
 *   <thead><tr><td><b>Annotation</b></td><td><b>Type</b></td></tr></thead>
 *   <tr><td>{@code @Parameter}</td><td>{@code PARAMETER}</td></tr>
 *   <tr><td>{@code @Parameters}</td><td>{@code PARAMETER}</td></tr>
 *   <tr><td>{@code @Option}</td><td>{@code OPTION}</td></tr>
 * </table>
 */
public enum ItemType {

  PARAMETER, OPTION
}
