package net.jbock.qualifier;

public class DescriptionKey {

  private final String descriptionKey;

  public DescriptionKey(String descriptionKey) {
    this.descriptionKey = descriptionKey;
  }

  public String key() {
    return descriptionKey;
  }
}
