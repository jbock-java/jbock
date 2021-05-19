package net.jbock.compiler.command;

import net.jbock.compiler.parameter.PositionalParameter;
import net.jbock.convert.ConvertedParameter;
import net.jbock.qualifier.SourceMethod;

import java.util.List;

class IntermediateResult {

  private final List<SourceMethod> options;
  private final List<ConvertedParameter<PositionalParameter>> positionalParameters;

  private IntermediateResult(
      List<SourceMethod> options,
      List<ConvertedParameter<PositionalParameter>> positionalParameters) {
    this.options = options;
    this.positionalParameters = positionalParameters;
  }

  static IntermediateResult create(
      List<SourceMethod> options,
      List<ConvertedParameter<PositionalParameter>> positionalParameters) {
    return new IntermediateResult(options, positionalParameters);
  }

  List<SourceMethod> options() {
    return options;
  }

  List<ConvertedParameter<PositionalParameter>> positionalParameters() {
    return positionalParameters;
  }
}
