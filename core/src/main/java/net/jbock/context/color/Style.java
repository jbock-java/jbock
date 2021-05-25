package net.jbock.context.color;

enum Style {

  BOLD(1),
  FG_RED(31);

  private final int code;

  Style(int code) {
    this.code = code;
  }

  int code() {
    return code;
  }
}
