package net.jbock.contrib;

import java.util.Optional;

final class AnsiStyle {

  private static final char ESC = 0x1B;
  private static final String CSI = ESC + "[";
  private static final String RESET = CSI + "m";

  private final boolean ansi;

  private AnsiStyle(boolean ansi) {
    this.ansi = ansi;
  }

  static AnsiStyle create(boolean ansi) {
    return new AnsiStyle(ansi);
  }

  private Optional<String> paint(String text, Style style) {
    if (!ansi) {
      return Optional.empty();
    }
    return Optional.of(CSI + style.code + 'm' + text + RESET);
  }

  Optional<String> bold(String text) {
    return paint(text, Style.BOLD);
  }

  String red(String text) {
    return paint(text, Style.FG_RED).orElse(text);
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
