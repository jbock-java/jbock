package net.jbock.validate;

import net.jbock.convert.ConvertedParameter;
import net.jbock.parameter.NamedOption;
import net.jbock.parameter.PositionalParameter;

import java.util.List;

public class Params {

  private final List<ConvertedParameter<PositionalParameter>> positionalParams;
  private final List<ConvertedParameter<NamedOption>> namedOptions;

  Params(
      List<ConvertedParameter<PositionalParameter>> positionalParams,
      List<ConvertedParameter<NamedOption>> namedOptions) {
    this.positionalParams = positionalParams;
    this.namedOptions = namedOptions;
  }

  public List<ConvertedParameter<PositionalParameter>> positionalParams() {
    return positionalParams;
  }

  public List<ConvertedParameter<NamedOption>> namedOptions() {
    return namedOptions;
  }
}
