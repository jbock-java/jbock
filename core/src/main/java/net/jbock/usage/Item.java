package net.jbock.usage;

import java.util.List;

public abstract class Item {

  private final String paramLabel;
  private final String descriptionKey;
  private final List<String> description;
  private final Skew skew;

  Item(String paramLabel, String descriptionKey, List<String> description, Skew skew) {
    this.paramLabel = paramLabel;
    this.descriptionKey = descriptionKey;
    this.description = description;
    this.skew = skew;
  }

  public abstract String name();

  public final String paramLabel() {
    return paramLabel;
  }

  public final List<String> description() {
    return description;
  }

  public final String descriptionKey() {
    return descriptionKey;
  }

  public final Skew skew() {
    return skew;
  }
}
