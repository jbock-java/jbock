package net.jbock.convert.reference;

import javax.lang.model.type.TypeMirror;
import java.util.List;

public class FunctionType {

  private final List<? extends TypeMirror> typeArguments;
  private final boolean supplier; // wrapped in Supplier?

  FunctionType(List<? extends TypeMirror> typeArguments, boolean supplier) {
    this.typeArguments = typeArguments;
    this.supplier = supplier;
  }

  public boolean isSupplier() {
    return supplier;
  }

  public TypeMirror outputType() {
    return typeArguments.get(1);
  }
}
