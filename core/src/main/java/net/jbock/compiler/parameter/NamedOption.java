package net.jbock.compiler.parameter;

import net.jbock.compiler.Description;
import net.jbock.compiler.EnumName;
import net.jbock.qualifier.DescriptionKey;
import net.jbock.qualifier.ParamLabel;
import net.jbock.qualifier.SourceMethod;

import java.util.List;
import java.util.Locale;

public class NamedOption extends AbstractParameter {

  private final List<String> dashedNames;

  public NamedOption(
      EnumName enumName,
      List<String> dashedNames,
      SourceMethod sourceMethod,
      DescriptionKey descriptionKey,
      Description description,
      ParamLabel paramLabel) {
    super(sourceMethod, enumName, descriptionKey, description, paramLabel);
    this.dashedNames = dashedNames;
  }

  public List<String> dashedNames() {
    return dashedNames;
  }

  public String dashedNamesWithLabel(boolean isFlag) {
    String sample = String.join(", ", dashedNames());
    return isFlag ? sample : sample + ' ' + paramLabel();
  }

  public String paramLabel() {
    return label().orElseGet(() -> dashedNames.stream()
        .filter(name -> name.startsWith("--"))
        .map(name -> name.substring(2))
        .map(s -> s.toUpperCase(Locale.US))
        .findFirst()
        .orElse(enumName().enumConstant().toUpperCase(Locale.ROOT)));
  }

  @Override
  public ParameterStyle style() {
    return ParameterStyle.OPTION;
  }

  public boolean hasUnixName() {
    return dashedNames.stream().anyMatch(s -> s.length() == 2);
  }
}
