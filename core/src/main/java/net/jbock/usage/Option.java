package net.jbock.usage;

import java.util.List;

public final class Option extends Item {

  private final List<String> names;
  private final boolean isFlag;

  public Option(
      String descriptionName,
      String descriptionKey,
      List<String> description,
      List<String> names,
      boolean isFlag,
      Skew skew) {
    super(descriptionName, descriptionKey, description, skew);
    this.names = names;
    this.isFlag = isFlag;
  }

  @Override
  public String name() {
    String sample = String.join(", ", names);
    return isFlag ? sample : sample + ' ' + paramLabel();
  }

  public List<String> names() {
    return names;
  }
}
