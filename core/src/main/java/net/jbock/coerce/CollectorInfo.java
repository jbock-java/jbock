package net.jbock.coerce;

import javax.lang.model.type.TypeMirror;
import java.util.Optional;

class CollectorInfo {

  final TypeMirror inputType;

  private final Optional<TypeMirror> collectorType;

  private CollectorInfo(TypeMirror inputType, Optional<TypeMirror> collectorType) {
    this.inputType = inputType;
    this.collectorType = collectorType;
  }

  static CollectorInfo create(TypeMirror inputType, TypeMirror collectorType) {
    return new CollectorInfo(inputType, Optional.of(collectorType));
  }

  static CollectorInfo listCollector(TypeMirror inputType) {
    return new CollectorInfo(inputType, Optional.empty());
  }

  Optional<TypeMirror> collectorType() {
    return collectorType;
  }

  @Override
  public String toString() {
    return "input: " + inputType;
  }
}
