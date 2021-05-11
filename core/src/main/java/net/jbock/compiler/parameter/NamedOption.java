package net.jbock.compiler.parameter;

import net.jbock.compiler.Description;
import net.jbock.compiler.EnumName;
import net.jbock.qualifier.DescriptionKey;
import net.jbock.qualifier.ParamLabel;
import net.jbock.qualifier.SourceMethod;

import java.util.List;
import java.util.Locale;

public class NamedOption extends AbstractParameter {

  private final List<String> names;
  private final ParamLabel paramLabel;

  public NamedOption(
      EnumName enumName,
      List<String> names,
      SourceMethod sourceMethod,
      DescriptionKey descriptionKey,
      Description description,
      ParamLabel paramLabel) {
    super(sourceMethod, enumName, descriptionKey, description);
    this.paramLabel = paramLabel;
    this.names = names;
  }

  public List<String> names() {
    return names;
  }

  public String dashedNamesWithLabel(boolean isFlag) {
    String sample = String.join(", ", names());
    return isFlag ? sample : sample + ' ' + paramLabel();
  }

  public String paramLabel() {
    return paramLabel.label().orElseGet(() -> names.stream()
        .filter(name -> name.startsWith("--"))
        .map(name -> name.substring(2))
        .map(s -> s.toUpperCase(Locale.US))
        .findFirst()
        .orElse(enumName().snake().toUpperCase(Locale.US)));
  }

  public boolean hasUnixName() {
    return names.stream().anyMatch(s -> s.length() == 2);
  }
}
