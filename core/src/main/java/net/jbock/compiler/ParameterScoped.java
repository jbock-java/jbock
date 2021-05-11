package net.jbock.compiler;

import javax.lang.model.util.Types;
import java.util.Arrays;
import java.util.List;

public class ParameterScoped {

  private final ParameterContext parameterContext;

  public ParameterScoped(ParameterContext parameterContext) {
    this.parameterContext = parameterContext;
  }

  public final TypeTool tool() {
    return parameterContext.tool;
  }

  public final Types types() {
    return tool().types();
  }

  public ParameterContext parameterContext() {
    return parameterContext;
  }

  public ParserFlavour flavour() {
    return parameterContext.flavour;
  }
}
