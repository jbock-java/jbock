package net.jbock.qualifier;

import java.util.Objects;
import java.util.Optional;

public class DescriptionKey {

  private final String descriptionKey;

  public DescriptionKey(String descriptionKey) {
    this.descriptionKey = Objects.toString(descriptionKey, "");
  }

  public Optional<String> key() {
    return descriptionKey.isEmpty() ? Optional.empty() : Optional.of(descriptionKey);
  }
}
