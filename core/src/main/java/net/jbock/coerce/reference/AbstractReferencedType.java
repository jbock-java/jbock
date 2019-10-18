package net.jbock.coerce.reference;

import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import java.util.Map;

// mapper or collector
public abstract class AbstractReferencedType {

  private final DeclaredType expectedType; // subtype of Function or Collector

  AbstractReferencedType(DeclaredType expectedType) {
    this.expectedType = expectedType;
  }

  public abstract Map<String, TypeMirror> mapTypevars(Map<String, TypeMirror> solution);

  public abstract boolean isSupplier();

  public DeclaredType expectedType() {
    return expectedType;
  }
}
