package net.jbock.coerce;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;

public class Extension {

  private final TypeElement baseClass;
  private final DeclaredType extensionClass;

  Extension(TypeElement baseClass, DeclaredType extensionClass) {
    this.baseClass = baseClass;
    this.extensionClass = extensionClass;
  }

  public TypeElement baseClass() {
    return baseClass;
  }

  public DeclaredType extensionClass() {
    return extensionClass;
  }

  @Override
  public String toString() {
    return String.format("%s extends %s", baseClass, extensionClass);
  }
}
