package net.jbock.coerce;

import net.jbock.com.squareup.javapoet.CodeBlock;

import javax.lang.model.type.TypeMirror;

import static java.util.Objects.requireNonNull;

public class CollectorInfo {

  public final CodeBlock collectorInit;

  public final TypeMirror collectorInput;

  CollectorInfo(CodeBlock collectorInit, TypeMirror collectorInput) {
    this.collectorInit = requireNonNull(collectorInit);
    this.collectorInput = collectorInput;
  }

  static CollectorInfo empty() {
    return new CollectorInfo(CodeBlock.builder().build(), null);
  }

  public boolean isEmpty() {
    return collectorInit.isEmpty();
  }

  @Override
  public String toString() {
    return "input: " + collectorInput;
  }
}
