package net.jbock.compiler.parameter;

import net.jbock.compiler.Description;
import net.jbock.compiler.EnumName;
import net.jbock.qualifier.DescriptionKey;
import net.jbock.qualifier.ParamLabel;
import net.jbock.qualifier.SourceMethod;

import java.util.Locale;

public class PositionalParameter extends AbstractParameter {

  private final int position;
  private final ParamLabel paramLabel;

  public PositionalParameter(
      SourceMethod sourceMethod,
      EnumName enumName,
      DescriptionKey descriptionKey,
      Description description,
      int position,
      ParamLabel paramLabel) {
    super(sourceMethod, enumName, descriptionKey, description);
    this.position = position;
    this.paramLabel = paramLabel;
  }

  public String paramLabel() {
    return paramLabel.label().orElse(enumName().snake().toUpperCase(Locale.US));
  }

  public int position() {
    return position;
  }
}
