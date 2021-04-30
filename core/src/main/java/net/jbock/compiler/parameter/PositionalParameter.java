package net.jbock.compiler.parameter;

import net.jbock.Param;
import net.jbock.compiler.EnumName;

import javax.lang.model.element.ExecutableElement;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.OptionalInt;

public class PositionalParameter extends Parameter {

  private final int positionalIndex;

  public PositionalParameter(
      ExecutableElement sourceMethod,
      EnumName enumName,
      String bundleKey,
      List<String> description,
      int positionalIndex) {
    super(sourceMethod, enumName, bundleKey, description);
    this.positionalIndex = positionalIndex;
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
  public String sample(boolean isFlag, EnumName enumName) {
    return enumName.snake().toLowerCase(Locale.US);
  }

  @Override
  public String optionName() {
    return null;
  }

  @Override
  public char mnemonic() {
    return ' ';
  }

  public int position() {
    return sourceMethod().getAnnotation(Param.class).value();
  }
}
