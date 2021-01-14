package net.jbock.coerce;

import dagger.Lazy;
import net.jbock.coerce.matching.AutoMatcher;
import net.jbock.coerce.matching.MapperMatcher;
import net.jbock.compiler.ParameterContext;
import net.jbock.compiler.ParameterScoped;

import javax.inject.Inject;

/**
 * Coercion input: Information about a single parameter (option or param).
 */
public class BasicInfo extends ParameterScoped {

  private final Lazy<MapperMatcher> mapperMatcher;
  private final Lazy<AutoMatcher> autoMatcher;

  @Inject
  BasicInfo(ParameterContext context, Lazy<MapperMatcher> mapperMatcher, Lazy<AutoMatcher> autoMatcher) {
    super(context);
    this.mapperMatcher = mapperMatcher;
    this.autoMatcher = autoMatcher;
  }

  public Coercion nonFlagCoercion() {
    return mapperClass()
        .map(mapper -> mapperMatcher.get().findMyCoercion(mapper))
        .orElseGet(() -> autoMatcher.get().findCoercion());
  }
}