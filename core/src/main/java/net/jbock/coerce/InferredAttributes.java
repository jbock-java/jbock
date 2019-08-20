package net.jbock.coerce;

import net.jbock.compiler.TypeTool;
import net.jbock.compiler.ValidationException;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.TypeMirror;
import java.util.List;
import java.util.Optional;

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
  private static boolean isInferredRepeatable(TypeMirror originalType) {
    return TypeTool.get().isSameErasure(originalType, List.class);
  }

  /**
   * Can infer {@code flag = true}?
   */
  public static boolean isInferredFlag(
      Object mapperClass,
      Object collectorClass,
      boolean flag,
      TypeMirror mirror) {
    if (mapperClass != null || collectorClass != null) {
      // no inferring
      return flag;
    }
    return flag || isInferredFlag(mirror);
  }

  private static boolean isInferredFlag(TypeMirror mirror) {
    TypeTool tool = TypeTool.get();
    return tool.isSameType(mirror, Boolean.class) || tool.isBooleanPrimitive(mirror);
  }

  public static InferredAttributes infer(
      Object mapperClass,
      Object collectorClass,
      boolean repeatable, // user declared
      boolean optional, // user declared
      TypeMirror mirror,
      ExecutableElement sourceMethod) {

    LiftedType liftedType = LiftedType.lift(mirror, TypeTool.get());
    Optional<TypeMirror> optionalInfo = findOptionalInfoInternal(TypeTool.get(), liftedType, sourceMethod);
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
        repeatable || isInferredRepeatable(mirror),
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

  LiftedType liftedType() {
    return liftedType;
  }

  public boolean repeatable() {
    return repeatable;
  }
}
