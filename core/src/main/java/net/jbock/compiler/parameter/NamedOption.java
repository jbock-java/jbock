package net.jbock.compiler.parameter;

import net.jbock.compiler.EnumName;

import javax.lang.model.element.ExecutableElement;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.OptionalInt;

public class NamedOption extends Parameter {

  private final String optionName;

  private final char mnemonic;

  public NamedOption(
      char mnemonic,
      String optionName,
      ExecutableElement sourceMethod,
      String bundleKey,
      List<String> description) {
    super(sourceMethod, bundleKey, description);
    this.mnemonic = mnemonic;
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
    if (optionName != null && mnemonic == ' ') {
      return Collections.singletonList("--" + optionName);
    } else if (optionName == null && mnemonic != ' ') {
      return Collections.singletonList("-" + mnemonic);
    } else if (optionName == null) {
      return Collections.emptyList();
    }
    return Arrays.asList("-" + mnemonic, "--" + optionName);
  }

  @Override
  public String sample(boolean isFlag, EnumName enumName) {
    List<String> names = dashedNames();
    if (names.isEmpty() || names.size() >= 3) {
      throw new AssertionError();
    }
    String sample = String.join(", ", names);
    return isFlag ? sample : sample + ' ' + enumName.enumConstant();
  }

  @Override
  public String optionName() {
    return optionName;
  }

  @Override
  public char mnemonic() {
    return mnemonic;
  }
}
