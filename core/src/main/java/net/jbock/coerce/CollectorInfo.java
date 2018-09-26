package net.jbock.coerce;

import net.jbock.com.squareup.javapoet.CodeBlock;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

public class CollectorInfo {

  private static final CodeBlock STANDARD_INIT = CodeBlock.builder().add("$T.toList()", Collectors.class).build();

  public final CodeBlock collectorInit;

  public final TypeMirror collectorInput;

  private CollectorInfo(CodeBlock collectorInit, TypeMirror collectorInput) {
    this.collectorInit = requireNonNull(collectorInit);
    this.collectorInput = collectorInput;
  }

  static CollectorInfo empty() {
    return new CollectorInfo(CodeBlock.builder().build(), null);
  }

  static CollectorInfo create(TypeMirror input, TypeElement collectorClass) {
    CodeBlock init = CodeBlock.builder()
        .add("new $T().get()", collectorClass)
        .build();
    return new CollectorInfo(init, input);
  }

  static CollectorInfo createWithStandardCollectorInit(TypeMirror input) {
    return new CollectorInfo(standardCollectorInit(), input);
  }

  static CodeBlock standardCollectorInit() {
    return STANDARD_INIT;
  }

  public boolean isEmpty() {
    return collectorInit.isEmpty();
  }

  @Override
  public String toString() {
    return "input: " + collectorInput;
  }
}
