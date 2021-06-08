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
   * The display name of this item, for usage documentation.
   *
   * @return item name, a non-empty string
   */
  public abstract String name();

  /**
   * The param label.
   * For unary options, this is the sample argument that's
   * printed in the usage documentation.
   *
   * @return param label, a non-empty string
   */
  public final String paramLabel() {
    return paramLabel;
  }

  /**
   * The default item description, for usage documentation.
   * Possibly empty.
   * May be overridden if {@link #descriptionKey()} is nonempty.
   *
   * @return description lines
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
