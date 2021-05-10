package net.jbock.compiler.parameter;

import net.jbock.Parameter;
import net.jbock.compiler.EnumName;
import net.jbock.qualifier.ConverterClass;

import javax.lang.model.element.ExecutableElement;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class PositionalParameter extends AbstractParameter {

  private final int positionalIndex;

  private final ParameterStyle style;

  public PositionalParameter(
      ExecutableElement sourceMethod,
      EnumName enumName,
      String descriptionKey,
      List<String> description,
      int positionalIndex,
      ConverterClass converter) {
    super(sourceMethod, enumName, descriptionKey, description, converter);
    this.positionalIndex = positionalIndex;
    this.style = sourceMethod().getAnnotation(Parameter.class) != null ?
        ParameterStyle.PARAMETER :
        ParameterStyle.PARAMETERS;
  }

  @Override
  public List<String> dashedNames() {
    return Collections.emptyList();
  }

  @Override
  public String descriptionSummary(boolean isFlag) {
    return enumName().snake().toUpperCase(Locale.US);
  }

  @Override
  public ParameterStyle style() {
    return style;
  }

  public int position() {
    return positionalIndex;
  }
}
