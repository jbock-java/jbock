package net.jbock.compiler.color;

enum Style {

  BOLD(1, 21),
  FG_RED(31, 39),
  FG_YELLOW(33, 39);

  private static final char ESC = 0x1B;
  private static final String CSI = ESC + "[";

  private final String on;
  private final String off;

  Style(int startCode, int endCode) {
    on = CSI + startCode + "m";
    off = CSI + endCode + "m";;
  }

  String on() {
    return on;
  }

  String off() {
    return off;
  }
}
