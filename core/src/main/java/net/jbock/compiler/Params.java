package net.jbock.compiler;

import net.jbock.compiler.parameter.NamedOption;
import net.jbock.compiler.parameter.PositionalParameter;
import net.jbock.convert.ConvertedParameter;

import java.util.List;

public class Params {

  public final List<ConvertedParameter<PositionalParameter>> positionalParams;
  public final List<ConvertedParameter<NamedOption>> namedOptions;

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
