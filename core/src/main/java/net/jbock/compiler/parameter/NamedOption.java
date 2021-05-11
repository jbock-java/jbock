package net.jbock.compiler.parameter;

import net.jbock.compiler.EnumName;
import net.jbock.qualifier.ConverterClass;
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
      List<String> description,
      ConverterClass converter,
      ParamLabel paramLabel) {
    super(sourceMethod, enumName, descriptionKey, description, converter, paramLabel);
    this.dashedNames = dashedNames;
  }

  @Override
  public List<String> dashedNames() {
    return dashedNames;
  }

  @Override
  public String descriptionSummary(boolean isFlag) {
    String sample = String.join(", ", dashedNames());
    return isFlag ? sample : sample + ' ' + descriptionArgName();
  }

  public String descriptionArgName() {
    return dashedNames.stream()
        .filter(name -> name.startsWith("--"))
        .map(name -> name.substring(2))
        .map(s -> s.toUpperCase(Locale.US))
        .findFirst()
        .orElse(enumName().enumConstant().toUpperCase(Locale.ROOT));
  }

  @Override
  public ParameterStyle style() {
    return ParameterStyle.OPTION;
  }

  public boolean hasUnixName() {
    return dashedNames.stream().anyMatch(s -> s.length() == 2);
  }
}
