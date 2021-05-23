package net.jbock.compiler.color;

import dagger.Reusable;
import net.jbock.qualifier.SourceElement;

import javax.inject.Inject;

@Reusable
public class Styler {

  private final SourceElement sourceElement;

  @Inject
  Styler(SourceElement sourceElement) {
    this.sourceElement = sourceElement;
  }

  private String paint(Style style, String text) {
    if (!sourceElement.isAnsi()) {
      return text;
    }
    return style.code() + text + Style.OFF;
  }

  public String red(String text) {
    return paint(Style.FG_RED, text);
  }

  public String bold(String text) {
    return paint(Style.BOLD, text);
  }

  public String yellow(String text) {
    return paint(Style.FG_YELLOW, text);
  }

  public String yellowOrQuote(String text) {
    if (sourceElement.isAnsi()) {
      return yellow(text);
    }
    return '"' + text + '"';
  }
}
