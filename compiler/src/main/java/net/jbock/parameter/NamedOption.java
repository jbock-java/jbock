package net.jbock.parameter;

import net.jbock.common.EnumName;
import net.jbock.common.SnakeName;
import net.jbock.either.Optional;

import java.util.List;
import java.util.Locale;

public final class NamedOption extends AbstractItem {

  private final List<String> names;

  public NamedOption(
      EnumName enumName,
      List<String> names,
      SourceMethod sourceMethod) {
    super(sourceMethod, enumName);
    this.names = names;
  }

  public List<String> names() {
    return names;
  }

  @Override
  public final String paramLabel() {
    return sourceMethod().paramLabel().or(() -> names.stream()
        .filter(name -> name.startsWith("--"))
        .map(name -> name.substring(2))
        .map(s -> s.toUpperCase(Locale.US))
        .findFirst()
        .map(Optional::of)
        .orElse(Optional.empty()))
        .orElse(SnakeName.create(methodName()).snake('_').toUpperCase(Locale.US));
  }

  public boolean hasUnixName() {
    return names.stream().anyMatch(s -> s.length() == 2);
  }
}
