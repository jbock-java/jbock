package net.jbock.model;

import java.util.ArrayList;
import java.util.List;

/**
 * A positional parameter, possibly repeatable.
 */
public final class Parameter extends Item {

  Parameter(
      String paramLabel,
      String descriptionKey,
      List<String> description,
      Multiplicity multiplicity) {
    super(paramLabel, descriptionKey, description, multiplicity);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    private String paramLabel;
    private String descriptionKey;
    private final List<String> description = new ArrayList<>();
    private Multiplicity skew;

    public Builder withParamLabel(String paramLabel) {
      this.paramLabel = paramLabel;
      return this;
    }

    public Builder withDescriptionKey(String descriptionKey) {
      this.descriptionKey = descriptionKey;
      return this;
    }

    public Builder addDescriptionLine(String descriptionLine) {
      this.description.add(descriptionLine);
      return this;
    }

    public Builder withMultiplicity(Multiplicity multiplicity) {
      this.skew = multiplicity;
      return this;
    }

    public Parameter build() {
      return new Parameter(paramLabel, descriptionKey, description, skew);
    }
  }

  @Override
  public String name() {
    return paramLabel();
  }
}
