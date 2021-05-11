package net.jbock.compiler.parameter;

import net.jbock.Parameter;
import net.jbock.compiler.Description;
import net.jbock.compiler.EnumName;
import net.jbock.qualifier.DescriptionKey;
import net.jbock.qualifier.ParamLabel;
import net.jbock.qualifier.SourceMethod;

import java.util.Locale;

public class PositionalParameter extends AbstractParameter {

  private final int positionalIndex;

  private final ParameterStyle style;

  public PositionalParameter(
      SourceMethod sourceMethod,
      EnumName enumName,
      DescriptionKey descriptionKey,
      Description description,
      int positionalIndex,
      ParamLabel paramLabel) {
    super(sourceMethod, enumName, descriptionKey, description, paramLabel);
    this.positionalIndex = positionalIndex;
    this.style = sourceMethod().getAnnotation(Parameter.class) != null ?
        ParameterStyle.PARAMETER :
        ParameterStyle.PARAMETERS;
  }

  @Override
  public ParameterStyle style() {
    return style;
  }

  public String paramLabel() {
    return label().orElse(enumName().snake().toUpperCase(Locale.US));
  }

  public int position() {
    return positionalIndex;
  }
}
