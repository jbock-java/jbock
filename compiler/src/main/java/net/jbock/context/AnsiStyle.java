package net.jbock.context;

import net.jbock.processor.SourceElement;

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

  private Optional<String> paint(String text, Style style) {
    if (!sourceElement.isAnsi()) {
      return Optional.empty();
    }
    return Optional.of(CSI + style.code + 'm' + text + RESET);
  }

  public String red(String text) {
    return paint(text, Style.FG_RED).orElse(text);
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
