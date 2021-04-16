package net.jbock.compiler;

import net.jbock.compiler.parameter.NamedOption;
import net.jbock.compiler.parameter.PositionalParameter;

import java.util.List;

class Params {

  final List<PositionalParameter> positionalParams;
  final List<NamedOption> namedOptions;

  Params(List<PositionalParameter> positionalParams, List<NamedOption> namedOptions) {
    this.positionalParams = positionalParams;
    this.namedOptions = namedOptions;
  }
}
