package net.jbock.coerce;

import net.jbock.coerce.matching.AutoMatcher;
import net.jbock.coerce.matching.MapperMatcher;
import net.jbock.coerce.matching.Matcher;
import net.jbock.compiler.ParameterContext;
import net.jbock.compiler.ParameterScoped;

import javax.inject.Inject;

public class CoercionProvider extends ParameterScoped {

  @Inject
  public CoercionProvider(ParameterContext parameterContext) {
    super(parameterContext);
  }

}
