package net.jbock.validate;

import net.jbock.parameter.SourceMethod;

import java.util.List;

public class AbstractMethods {

  private final List<SourceMethod> positionalParameters;
  private final List<SourceMethod> namedOptions;

  AbstractMethods(List<SourceMethod> positionalParameters, List<SourceMethod> namedOptions) {
    this.positionalParameters = positionalParameters;
    this.namedOptions = namedOptions;
  }

  public List<SourceMethod> positionalParameters() {
    return positionalParameters;
  }

  public List<SourceMethod> namedOptions() {
    return namedOptions;
  }
}
