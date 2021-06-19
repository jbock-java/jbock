package net.jbock.model;

import net.jbock.util.ItemType;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Runtime model of a positional parameter.
 */
public final class Parameter extends Item {

  Parameter(
      String paramLabel,
      String descriptionKey,
      List<String> description,
      Multiplicity multiplicity) {
    super(paramLabel, descriptionKey, description, multiplicity);
  }

  /**
   * Creates a builder instance.
   * Public method that may be invoked from the generated code.
   *
   * @return empty builder
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * Builder for a {@link Parameter}.
   */
  public static final class Builder {

    private String paramLabel;
    private String descriptionKey = "";
    private final List<String> description = new ArrayList<>();
    private Multiplicity multiplicity = Multiplicity.REQUIRED;

    private Builder() {
    }

    /**
     * Set the param label.
     * Public method that may be invoked from the generated code.
     *
     * @param paramLabel a non-empty string
     * @return the builder instance
     */
    public Builder withParamLabel(String paramLabel) {
      this.paramLabel = paramLabel;
      return this;
    }

    /**
     * Set the description key.
     * Public method that may be invoked from the generated code.
     *
     * @param descriptionKey a string, possibly empty
     * @return the builder instance
     */
    public Builder withDescriptionKey(String descriptionKey) {
      this.descriptionKey = descriptionKey;
      return this;
    }

    /**
     * Add a line of description text.
     * Public method that may be invoked from the generated code.
     *
     * @param descriptionLine a string, possibly empty
     * @return the builder instance
     */
    public Builder addDescriptionLine(String descriptionLine) {
      this.description.add(descriptionLine);
      return this;
    }

    /**
     * Set the multiplicity of this option.
     * Public method that may be invoked from the generated code.
     *
     * @param multiplicity the multiplicity
     * @return the builder instance
     */
    public Builder withMultiplicity(Multiplicity multiplicity) {
      this.multiplicity = multiplicity;
      return this;
    }

    /**
     * Create the model for this parameter.
     * Public method that may be invoked from the generated code.
     *
     * @return parameter model
     */
    public Parameter build() {
      return new Parameter(paramLabel, descriptionKey, description, multiplicity);
    }
  }

  @Override
  public String namesOverview() {
    return paramLabel();
  }

  @Override
  public String namesOverviewError() {
    return itemType().name().toLowerCase(Locale.US) + " " + paramLabel();
  }

  @Override
  public ItemType itemType() {
    return ItemType.PARAMETER;
  }
}
