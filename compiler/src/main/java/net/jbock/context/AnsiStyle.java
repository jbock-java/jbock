package net.jbock.context;

import net.jbock.processor.SourceElement;
import net.jbock.context.ContextScope;

import javax.inject.Inject;
import java.util.Optional;

@ContextScope
public class AnsiStyle {

  private static final char ESC = 0x1B;
  private static final String CSI = ESC + "[";
  private static final String RESET = CSI + "m";

  private final SourceElement sourceElement;

  @Inject
  AnsiStyle(SourceElement sourceElement) {
    this.sourceElement = sourceElement;
  }

  private Optional<String> paint(String text, Style... styles) {
    if (!sourceElement.isAnsi()) {
      return Optional.empty();
    }
    StringBuilder sb = new StringBuilder();
    sb.append(CSI);
    for (int i = 0; i < styles.length; i++) {
      if (i > 0) {
        sb.append(';');
      }
      sb.append(styles[i].code);
    }
    sb.append('m');
    sb.append(text);
    sb.append(RESET);
    return Optional.of(sb.toString());
  }

  public Optional<String> boldRed(String text) {
    return paint(text, Style.FG_RED, Style.BOLD);
  }

  public Optional<String> bold(String text) {
    return paint(text, Style.BOLD);
  }

  private enum Style {

    BOLD(1),
    FG_RED(31);

    final int code;

    Style(int code) {
      this.code = code;
    }
  }
}
