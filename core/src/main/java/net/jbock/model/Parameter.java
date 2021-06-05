package net.jbock.model;

import java.util.ArrayList;
import java.util.List;

/**
 * A positional parameter.
 */
public final class Parameter extends Item {

  Parameter(
      String paramLabel,
      String descriptionKey,
      List<String> description,
      Skew skew) {
    super(paramLabel, descriptionKey, description, skew);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    private String paramLabel;
    private String descriptionKey;
    private final List<String> description = new ArrayList<>();
    private Skew skew;

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

    public Builder withSkew(Skew skew) {
      this.skew = skew;
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
