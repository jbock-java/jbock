package net.jbock.model;

import java.util.ArrayList;
import java.util.List;

/**
 * A named option or a modal flag.
 */
public final class Option extends Item {

  private final List<String> names;

  Option(
      String paramLabel,
      String descriptionKey,
      List<String> description,
      List<String> names,
      Skew skew) {
    super(paramLabel, descriptionKey, description, skew);
    this.names = names;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {

    private String paramLabel;
    private String descriptionKey;
    private final List<String> description = new ArrayList<>();
    private List<String> names;
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

    public Builder withNames(List<String> names) {
      this.names = names;
      return this;
    }

    public Builder withSkew(Skew skew) {
      this.skew = skew;
      return this;
    }

    public Option build() {
      return new Option(paramLabel, descriptionKey, description, names, skew);
    }
  }

  @Override
  public String name() {
    String sample = String.join(", ", names);
    if (skew() == Skew.MODAL_FLAG) {
      return sample;
    }
    return sample + ' ' + paramLabel();
  }

  public List<String> names() {
    return names;
  }
}
