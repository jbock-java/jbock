package net.jbock.compiler;

import net.jbock.coerce.Coercion;
import net.jbock.compiler.parameter.NamedOption;
import net.jbock.compiler.parameter.PositionalParameter;

import java.util.List;

class Params {

  final List<Coercion<PositionalParameter>> positionalParams;
  final List<Coercion<NamedOption>> namedOptions;

  Params(
      List<Coercion<PositionalParameter>> positionalParams,
      List<Coercion<NamedOption>> namedOptions) {
    this.positionalParams = positionalParams;
    this.namedOptions = namedOptions;
  }
}
