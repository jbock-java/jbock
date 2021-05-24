package net.jbock.compiler.color;

enum Style {

  BOLD(1),
  FG_RED(31),
  FG_YELLOW(33);

  private final int code;

  Style(int code) {
    this.code = code;
  }

  int code() {
    return code;
  }
}
