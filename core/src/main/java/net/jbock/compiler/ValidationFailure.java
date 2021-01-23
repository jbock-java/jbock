package net.jbock.compiler;

import javax.lang.model.element.Element;

public class ValidationFailure {

  private final Element about;
  private final String message;

  public ValidationFailure(String message, Element about) {
    this.message = message;
    this.about = about;
  }

  public Element about() {
    return about;
  }

  public String message() {
    return message;
  }
}
