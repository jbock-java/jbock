package net.jbock.compiler.parameter;

import net.jbock.compiler.EnumName;

import javax.lang.model.element.ExecutableElement;
import java.util.List;
import java.util.OptionalInt;

public class NamedOption extends AbstractParameter {

  private final List<String> optionName;

  public NamedOption(
      EnumName enumName,
      List<String> optionName,
      ExecutableElement sourceMethod,
      String bundleKey,
      List<String> description) {
    super(sourceMethod, enumName, bundleKey, description);
    this.optionName = optionName;
  }

  @Override
  public boolean isPositional() {
    return false;
  }

  @Override
  public OptionalInt positionalIndex() {
    return OptionalInt.empty();
  }

  @Override
  public List<String> dashedNames() {
    return optionName;
  }

  @Override
  public String sample(boolean isFlag, EnumName enumName) {
    String sample = String.join(", ", dashedNames());
    return isFlag ? sample : sample + ' ' + enumName.enumConstant();
  }

  @Override
  public ParameterStyle style() {
    return ParameterStyle.OPTION;
  }

  public boolean hasUnixName() {
    return optionName.stream().anyMatch(s -> s.length() == 2);
  }
}
