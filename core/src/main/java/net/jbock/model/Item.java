package net.jbock.model;

import java.util.List;

/**
 * Abstract superclass of {@link Option} and {@link Parameter}.
 */
public abstract class Item {

  private final String paramLabel;
  private final String descriptionKey;
  private final List<String> description;
  private final Multiplicity multiplicity;

  Item(
      String paramLabel,
      String descriptionKey,
      List<String> description,
      Multiplicity multiplicity) {
    this.paramLabel = paramLabel;
    this.descriptionKey = descriptionKey;
    this.description = description;
    this.multiplicity = multiplicity;
  }

  /**
   * An overview of all names of this item, including a
   * sample argument if this is a unary option.
   * Used in the LHS of the usage documentation.
   *
   * @return item name, a non-empty string
   */
  public abstract String namesOverview();

  /**
   * The item as shown in the synopsis, if this is a
   * positional parameter or a required option.
   * For unary options, this is also the name of the
   * sample argument that's shown in the full usage
   * documentation.
   *
   * @return param label, a non-empty string
   */
  public final String paramLabel() {
    return paramLabel;
  }

  /**
   * Get the description that is present directly on the annotated method,
   * either as a description attribute, or in the form of javadoc.
   *
   * @return a list of lines, possibly empty
   */
  public final List<String> description() {
    return description;
  }

  /**
   * A string, possibly empty.
   *
   * @return description key
   */
  public final String descriptionKey() {
    return descriptionKey;
  }

  /**
   * The multiplicity of this item.
   *
   * @return item multiplicity
   */
  public final Multiplicity multiplicity() {
    return multiplicity;
  }
}
