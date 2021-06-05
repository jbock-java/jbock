package net.jbock.usage;

import net.jbock.model.UsageContext;

import java.util.Optional;

public class AnsiStyle {

  private static final char ESC = 0x1B;
  private static final String CSI = ESC + "[";
  private static final String RESET = CSI + "m";

  private final boolean ansi;

  AnsiStyle(boolean ansi) {
    this.ansi = ansi;
  }

  static AnsiStyle create(UsageContext context) {
    return new AnsiStyle(context.ansi());
  }

  private Optional<String> paint(String text, Style style) {
    if (!ansi) {
      return Optional.empty();
    }
    return Optional.of(CSI + style.code + 'm' + text + RESET);
  }

  String bold(String text) {
    return paint(text, Style.BOLD).orElse(text);
  }

  private enum Style {

    BOLD(1);

    final int code;

    Style(int code) {
      this.code = code;
    }
  }
}
