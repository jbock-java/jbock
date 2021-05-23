package net.jbock.compiler.color;

import dagger.Reusable;
import net.jbock.qualifier.SourceElement;

import javax.inject.Inject;
import java.util.Optional;

@Reusable
public class Styler {

  private final SourceElement sourceElement;

  @Inject
  Styler(SourceElement sourceElement) {
    this.sourceElement = sourceElement;
  }

  private Optional<String> paint(String text, Style... styles) {
    if (!sourceElement.isAnsi()) {
      return Optional.empty();
    }
    StringBuilder sb = new StringBuilder();
    sb.append(Style.CSI);
    for (int i = 0; i < styles.length; i++) {
      if (i > 0) {
        sb.append(';');
      }
      sb.append(styles[i].code());
    }
    sb.append('m');
    sb.append(text);
    sb.append(Style.OFF);
    return Optional.of(sb.toString());
  }

  public Optional<String> boldRed(String text) {
    return paint(text, Style.FG_RED, Style.BOLD);
  }

  public Optional<String> bold(String text) {
    return paint(text, Style.BOLD);
  }

  public Optional<String> yellow(String text) {
    return paint(text, Style.FG_YELLOW);
  }
}
