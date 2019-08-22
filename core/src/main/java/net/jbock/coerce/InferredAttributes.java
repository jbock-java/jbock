package net.jbock.coerce;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.ParameterSpec;
import net.jbock.compiler.TypeTool;
import net.jbock.compiler.ValidationException;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.TypeMirror;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class InferredAttributes {

  private final boolean repeatable;

  private final Optional<TypeMirror> optionalInfo;

  private final LiftedType liftedType;

  private InferredAttributes(
      boolean repeatable,
      Optional<TypeMirror> optionalInfo,
      LiftedType liftedType) {
    this.repeatable = repeatable;
    this.optionalInfo = optionalInfo;
    this.liftedType = liftedType;
  }

  /**
   * Can infer {@code repeatable = true}?
   */
  private static boolean isInferredRepeatable(TypeTool tool, TypeMirror originalType) {
    return tool.isSameErasure(originalType, List.class);
  }

  public static InferredAttributes infer(
      Object mapperClass,
      Object collectorClass,
      boolean repeatable, // user declared
      boolean optional, // user declared
      TypeMirror originalReturnType,
      ExecutableElement sourceMethod,
      TypeTool tool) {

    LiftedType liftedType = LiftedType.lift(originalReturnType, tool);
    Optional<TypeMirror> optionalInfo = findOptionalInfoInternal(tool, liftedType, sourceMethod);
    if (optional && !optionalInfo.isPresent()) {
      throw ValidationException.create(sourceMethod, "Wrap the parameter type in Optional.");
    }
    if (mapperClass != null || collectorClass != null) {
      // no inferring
      if (optionalInfo.isPresent() && !optional) {
        throw ValidationException.create(sourceMethod, "Declare this parameter optional.");
      }
      return new InferredAttributes(repeatable, optionalInfo, liftedType);
    }
    return new InferredAttributes(
        repeatable || isInferredRepeatable(tool, originalReturnType),
        optionalInfo, liftedType);
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

  public boolean optional() {
    return optionalInfo.isPresent();
  }

  Optional<TypeMirror> optionalInfo() {
    return optionalInfo;
  }

  TypeMirror liftedType() {
    return liftedType.liftedType();
  }

  // liftedType -> sourceMethod.returnType
  Function<ParameterSpec, CodeBlock> extractExpr() {
    return liftedType.extractExpr();
  }

  public boolean repeatable() {
    return repeatable;
  }
}
