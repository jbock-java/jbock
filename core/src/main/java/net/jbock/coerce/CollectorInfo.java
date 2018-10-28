package net.jbock.coerce;

import com.squareup.javapoet.CodeBlock;

import javax.lang.model.type.TypeMirror;
import java.util.Optional;
import java.util.stream.Collectors;

public class CollectorInfo {

  private static final CodeBlock STANDARD_INIT = CodeBlock.builder().add("$T.toList()", Collectors.class).build();

  public final TypeMirror inputType;

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

  public CodeBlock collectorInit() {
    if (!collectorType.isPresent()) {
      return STANDARD_INIT;
    }
    return CodeBlock.builder()
        .add("new $T().get()", collectorType.get())
        .build();
  }

  static CodeBlock standardCollectorInit() {
    return STANDARD_INIT;
  }

  // visible for testing
  Optional<TypeMirror> collectorType() {
    return collectorType;
  }

  @Override
  public String toString() {
    return "input: " + inputType;
  }
}
