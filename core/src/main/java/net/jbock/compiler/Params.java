package net.jbock.compiler;

import net.jbock.convert.ConvertedParameter;
import net.jbock.compiler.parameter.NamedOption;
import net.jbock.compiler.parameter.PositionalParameter;

import java.util.List;

class Params {

  final List<ConvertedParameter<PositionalParameter>> positionalParams;
  final List<ConvertedParameter<NamedOption>> namedOptions;

  Params(
      List<ConvertedParameter<PositionalParameter>> positionalParams,
      List<ConvertedParameter<NamedOption>> namedOptions) {
    this.positionalParams = positionalParams;
    this.namedOptions = namedOptions;
  }
}
