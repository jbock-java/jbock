package net.jbock.convert;

import dagger.Lazy;
import net.jbock.compiler.parameter.AbstractParameter;
import net.jbock.convert.matching.auto.AutoConverterFinder;
import net.jbock.convert.matching.explicit.ExplicitConverterValidator;
import net.jbock.either.Either;
import net.jbock.qualifier.ConverterClass;
import net.jbock.scope.ParameterScope;

import javax.inject.Inject;

@ParameterScope
public class ConverterFinder {

  private final Lazy<AutoConverterFinder> autoConverterFinder;
  private final Lazy<ExplicitConverterValidator> converterValidator;
  private final ConverterClass converterClass;

  @Inject
  ConverterFinder(
      Lazy<AutoConverterFinder> autoConverterFinder,
      Lazy<ExplicitConverterValidator> converterValidator,
      ConverterClass converterClass) {
    this.autoConverterFinder = autoConverterFinder;
    this.converterValidator = converterValidator;
    this.converterClass = converterClass;
  }

  public <P extends AbstractParameter> Either<String, ConvertedParameter<P>> findConverter(P parameter) {
    return converterClass.converter()
        .map(converter -> converterValidator.get().validate(parameter, converter))
        .orElseGet(() -> autoConverterFinder.get().findConverter(parameter));
  }
}