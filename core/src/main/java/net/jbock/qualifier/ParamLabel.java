package net.jbock.qualifier;

import java.util.Objects;
import java.util.Optional;

public class ParamLabel {

  private final Optional<String> label;

  public ParamLabel(String label) {
    this.label = Objects.toString(label, "").isEmpty() ?
        Optional.empty() :
        Optional.of(label);
  }

  public Optional<String> label() {
    return label;
  }
}
