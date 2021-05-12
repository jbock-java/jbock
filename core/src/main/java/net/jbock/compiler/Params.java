package net.jbock.compiler;

import net.jbock.compiler.parameter.NamedOption;
import net.jbock.compiler.parameter.PositionalParameter;
import net.jbock.convert.ConvertedParameter;

import java.util.List;

public class Params {

  private final List<ConvertedParameter<PositionalParameter>> positionalParams;
  private final List<ConvertedParameter<NamedOption>> namedOptions;

  private Params(
      List<ConvertedParameter<PositionalParameter>> positionalParams,
      List<ConvertedParameter<NamedOption>> namedOptions) {
    this.positionalParams = positionalParams;
    this.namedOptions = namedOptions;
  }

  static Params create(List<ConvertedParameter<PositionalParameter>> positionalParams,
                       List<ConvertedParameter<NamedOption>> namedOptions) {
    return new Params(positionalParams, namedOptions);
  }

  public List<ConvertedParameter<PositionalParameter>> positionalParams() {
    return positionalParams;
  }

  public List<ConvertedParameter<NamedOption>> namedOptions() {
    return namedOptions;
  }
}
