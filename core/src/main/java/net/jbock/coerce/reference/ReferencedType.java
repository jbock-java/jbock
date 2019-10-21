package net.jbock.coerce.reference;

import javax.lang.model.type.TypeMirror;
import java.util.List;

// mapper or collector
public abstract class ReferencedType<E> {

  private final Declared<E> expectedType; // erases to Function or Collector (see ExpectedType)

  ReferencedType(Declared<E> expectedType) {
    this.expectedType = expectedType;
  }

  public abstract boolean isSupplier();

  public Declared<E> expectedType() {
    return expectedType;
  }

  public List<? extends TypeMirror> typeArguments() {
    return expectedType.typeArguments();
  }
}
