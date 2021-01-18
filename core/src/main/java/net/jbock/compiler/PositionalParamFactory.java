package net.jbock.compiler;

import net.jbock.coerce.BasicInfo;
import net.jbock.coerce.Coercion;
import net.jbock.compiler.parameter.Parameter;
import net.jbock.compiler.parameter.PositionalParameter;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;

class PositionalParamFactory extends ParameterScoped {

  private final BasicInfo basicInfo;

  @Inject
  PositionalParamFactory(ParameterContext parameterContext, BasicInfo basicInfo) {
    super(parameterContext);
    this.basicInfo = basicInfo;
  }

  Parameter createPositionalParam(int positionalIndex) {
    checkBundleKey();
    Coercion coercion = basicInfo.coercion();
    return new PositionalParameter(sourceMethod(), bundleKey(), enumName().snake().toLowerCase(Locale.US),
        Collections.emptyList(), coercion, Arrays.asList(description()), positionalIndex);
  }
}
