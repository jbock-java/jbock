package net.jbock.usage;

import java.util.List;

public final class Parameter extends Item {

  public Parameter(
      String descriptionName,
      String descriptionKey,
      List<String> description,
      Skew skew) {
    super(descriptionName, descriptionKey, description, skew);
  }

  @Override
  public String name() {
    return paramLabel();
  }
}
