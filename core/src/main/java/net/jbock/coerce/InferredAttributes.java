package net.jbock.coerce;

import net.jbock.compiler.TypeTool;
import net.jbock.compiler.ValidationException;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.TypeMirror;
import java.util.List;
import java.util.Optional;

public class InferredAttributes {

  // the X inside Optional<X>
  private final Optional<TypeMirror> optionalInfo;

  private final LiftedType liftedType;

  private InferredAttributes(
      Optional<TypeMirror> optionalInfo,
      LiftedType liftedType) {
    this.optionalInfo = optionalInfo;
    this.liftedType = liftedType;
  }

  public static InferredAttributes infer(
      Object mapperClass,
      Object collectorClass,
      TypeMirror originalReturnType,
      ExecutableElement sourceMethod,
      TypeTool tool) {

    LiftedType liftedType = LiftedType.lift(originalReturnType, tool);
    Optional<TypeMirror> optionalInfo = findOptionalInfoInternal(tool, liftedType, sourceMethod);
    if (mapperClass != null || collectorClass != null) {
      // no inferring
      return new InferredAttributes(optionalInfo, liftedType);
    }
    return new InferredAttributes(optionalInfo, liftedType);
  }

  private static Optional<TypeMirror> findOptionalInfoInternal(
      TypeTool tool,
      LiftedType liftedType,
      ExecutableElement sourceMethod) {
    TypeMirror returnType = liftedType.liftedType();
    if (!tool.isSameErasure(returnType, Optional.class)) {
      return Optional.empty();
    }
    List<? extends TypeMirror> typeArgs = tool.typeargs(returnType);
    if (typeArgs.isEmpty()) {
      throw ValidationException.create(sourceMethod, "Add a type parameter");
    }
    return Optional.of(typeArgs.get(0));
  }

  Optional<TypeMirror> optionalInfo() {
    return optionalInfo;
  }

  TypeMirror liftedType() {
    return liftedType.liftedType();
  }
}
