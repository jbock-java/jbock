package net.jbock.validate;

import net.jbock.parameter.SourceMethod;

import java.util.List;

class AbstractMethods {

  private final List<SourceMethod> positionalParameters;
  private final List<SourceMethod> namedOptions;

  AbstractMethods(List<SourceMethod> positionalParameters, List<SourceMethod> namedOptions) {
    this.positionalParameters = positionalParameters;
    this.namedOptions = namedOptions;
  }

  List<SourceMethod> positionalParameters() {
    return positionalParameters;
  }

  List<SourceMethod> namedOptions() {
    return namedOptions;
  }
}
