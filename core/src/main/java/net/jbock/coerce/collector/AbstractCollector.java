package net.jbock.coerce.collector;

import javax.lang.model.type.TypeMirror;

public abstract class AbstractCollector {

  // For custom collector this is the T in Collector<T, A, R>.
  // For default collector it is the E in List<E>.
  private final TypeMirror inputType;

  AbstractCollector(TypeMirror inputType) {
    this.inputType = inputType;
  }

  public TypeMirror inputType() {
    return inputType;
  }
}
