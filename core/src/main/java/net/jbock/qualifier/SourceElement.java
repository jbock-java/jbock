package net.jbock.qualifier;

import javax.lang.model.element.TypeElement;

public class SourceElement {

  private final TypeElement sourceElement;

  public SourceElement(TypeElement sourceElement) {
    this.sourceElement = sourceElement;
  }

  public TypeElement element() {
    return sourceElement;
  }
}
