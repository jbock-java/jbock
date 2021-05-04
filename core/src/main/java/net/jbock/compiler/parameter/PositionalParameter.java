package net.jbock.compiler.parameter;

import net.jbock.Parameter;
import net.jbock.compiler.EnumName;

import javax.lang.model.element.ExecutableElement;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.OptionalInt;

public class PositionalParameter extends AbstractParameter {

  private final int positionalIndex;

  private final ParameterStyle isParameter;

  public PositionalParameter(
      ExecutableElement sourceMethod,
      EnumName enumName,
      String bundleKey,
      List<String> description,
      int positionalIndex) {
    super(sourceMethod, enumName, bundleKey, description);
    this.positionalIndex = positionalIndex;
    this.isParameter = sourceMethod().getAnnotation(Parameter.class) != null ?
        ParameterStyle.PARAMETER :
        ParameterStyle.PARAMETERS;
  }

  @Override
  public boolean isPositional() {
    return true;
  }

  @Override
  public OptionalInt positionalIndex() {
    return OptionalInt.of(positionalIndex);
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
    return isParameter;
  }

  public int position() {
    return positionalIndex;
  }
}
