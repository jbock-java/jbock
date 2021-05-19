package net.jbock.compiler;

import net.jbock.qualifier.SourceMethod;

import java.util.List;

public class Methods {

  private final List<SourceMethod> params;
  private final List<SourceMethod> options;

  Methods(List<SourceMethod> params, List<SourceMethod> options) {
    this.params = params;
    this.options = options;
  }

  public List<SourceMethod> params() {
    return params;
  }

  public List<SourceMethod> options() {
    return options;
  }
}
