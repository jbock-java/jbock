package net.jbock.compiler.color;

import dagger.Reusable;

import javax.inject.Inject;

@Reusable
public class Styler {

  @Inject
  Styler() {
  }

  private String paint(Style style, String text) {
    return style.on() + text + style.off();
  }

  public String startRed() {
    return Style.FG_RED.on();
  }

  public String endRed() {
    return Style.FG_RED.off();
  }

  public String bold(String text) {
    return paint(Style.BOLD, text);
  }

  public String yellow(String text) {
    return paint(Style.FG_YELLOW, text);
  }
}
