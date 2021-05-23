package net.jbock.compiler.color;

enum Style {

  BOLD(1, 21),
  FG_RED(31, 39),
  FG_YELLOW(33, 39);

  // control sequence introducer
  private static final String CSI = "\u001B[";

  private final int startCode;
  private final int endCode;

  Style(int startCode, int endCode) {
    this.startCode = startCode;
    this.endCode = endCode;
  }

  String on() {
    return CSI + startCode + "m";
  }

  String off() {
    return CSI + endCode + "m";
  }
}
