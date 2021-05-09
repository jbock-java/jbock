package net.jbock.convert;

import dagger.Lazy;
import net.jbock.convert.matching.auto.AutoConverterFinder;
import net.jbock.convert.matching.explicit.ExplicitConverterFinder;
import net.jbock.compiler.ParameterContext;
import net.jbock.compiler.ParameterScoped;
import net.jbock.compiler.parameter.AbstractParameter;
import net.jbock.either.Either;

import javax.inject.Inject;

/**
 * Coercion input: Information about a single parameter (option or param).
 */
public class BasicInfo extends ParameterScoped {

  private final Lazy<AutoConverterFinder> autoMatcher;
  private final Lazy<ExplicitConverterFinder> mapperMatcher;

  @Inject
  BasicInfo(
      ParameterContext context,
      Lazy<AutoConverterFinder> autoMatcher,
      Lazy<ExplicitConverterFinder> mapperMatcher) {
    super(context);
    this.autoMatcher = autoMatcher;
    this.mapperMatcher = mapperMatcher;
  }

  public <P extends AbstractParameter> Either<String, ConvertedParameter<P>> coercion(P parameter) {
    return parameter.converter()
        .map(mapper -> mapperMatcher.get().findConverter(parameter, mapper))
        .orElseGet(() -> autoMatcher.get().findConverter(parameter));
  }
}