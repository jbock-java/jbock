package net.jbock.model;

import java.util.ArrayList;
import java.util.List;

/**
 * A named option or a modal flag.
 */
public final class Option extends Item {

  private final List<String> names;
  private final boolean modeFlag;

  Option(
      String paramLabel,
      String descriptionKey,
      List<String> description,
      List<String> names,
      Multiplicity multiplicity,
      boolean modeFlag) {
    super(paramLabel, descriptionKey, description, multiplicity);
    this.names = names;
    this.modeFlag = modeFlag;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {

    private String paramLabel;
    private String descriptionKey;
    private final List<String> description = new ArrayList<>();
    private List<String> names;
    private Multiplicity multiplicity;
    private boolean modeFlag;

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

    public Builder withMultiplicity(Multiplicity multiplicity) {
      this.multiplicity = multiplicity;
      return this;
    }

    public Builder withModeFlag() {
      this.multiplicity = Multiplicity.OPTIONAL;
      this.modeFlag = true;
      return this;
    }

    public Option build() {
      return new Option(paramLabel, descriptionKey, description, names, multiplicity, modeFlag);
    }
  }

  @Override
  public String name() {
    String sample = String.join(", ", names);
    if (modeFlag) {
      return sample;
    }
    return sample + ' ' + paramLabel();
  }

  public List<String> names() {
    return names;
  }
}
