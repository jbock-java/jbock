package net.jbock.coerce;

import net.jbock.coerce.matching.AutoMatcher;
import net.jbock.coerce.matching.MapperMatcher;
import net.jbock.compiler.ParameterContext;
import net.jbock.compiler.ParameterScoped;

import javax.inject.Inject;

/**
 * Coercion input: Information about a single parameter (option or param).
 */
@Deprecated
public class BasicInfo extends ParameterScoped {

  private final MapperMatcher mapperMatcher;
  private final AutoMatcher autoMatcher;

  @Inject
  BasicInfo(ParameterContext context, MapperMatcher mapperMatcher, AutoMatcher autoMatcher) {
    super(context);
    this.mapperMatcher = mapperMatcher;
    this.autoMatcher = autoMatcher;
  }

  public Coercion nonFlagCoercion() {
    return mapperClass()
        .map(mapperMatcher::findCoercion)
        .orElseGet(autoMatcher::findCoercion);
  }
}