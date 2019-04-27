package net.jbock.coerce;

import net.jbock.compiler.TypeTool;

import javax.lang.model.type.TypeMirror;
import java.util.List;

public class Infer {

  public final boolean repeatable;
  public final boolean optional;

  private Infer(boolean repeatable, boolean optional) {
    this.repeatable = repeatable;
    this.optional = optional;
  }

  /**
   * Can infer {@code optional = true}?
   */
  private static boolean isInferredOptional(TypeMirror mirror) {
    return LiftedType.liftsToOptional(mirror);
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

  public static Infer infer(
      Object mapperClass,
      Object collectorClass,
      boolean repeatable, // user declared
      boolean optional, // user declared
      TypeMirror mirror) {

    if (mapperClass != null || collectorClass != null) {
      // no inferring
      return new Infer(repeatable, optional);
    }
    return new Infer(
        repeatable || isInferredRepeatable(mirror),
        optional || isInferredOptional(mirror));
  }
}
