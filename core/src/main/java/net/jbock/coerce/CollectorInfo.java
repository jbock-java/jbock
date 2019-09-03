package net.jbock.coerce;

import javax.lang.model.type.TypeMirror;
import java.util.Optional;

public class CollectorInfo {

  // the T in Collector<T, A, R>; this is clear from the return type of the mapper function
  final TypeMirror inputType;

  private final Optional<CollectorType> collectorType;

  private CollectorInfo(TypeMirror inputType, Optional<CollectorType> collectorType) {
    this.inputType = inputType;
    this.collectorType = collectorType;
  }

  static CollectorInfo create(TypeMirror inputType, CollectorType collectorType) {
    return new CollectorInfo(inputType, Optional.of(collectorType));
  }

  static CollectorInfo listCollector(TypeMirror inputType) {
    return new CollectorInfo(inputType, Optional.empty());
  }

  Optional<CollectorType> collectorType() {
    return collectorType;
  }
}
