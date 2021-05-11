package net.jbock.qualifier;

import java.util.Objects;
import java.util.Optional;

public class ParamLabel {

  private final String label;

  public ParamLabel(String label) {
    this.label = Objects.toString(label, "");
  }

  public Optional<String> label() {
    return label.isEmpty() ? Optional.empty() : Optional.of(label);
  }
}
