package net.jbock.coerce;

import net.jbock.compiler.TypeTool;

import javax.lang.model.type.TypeMirror;

public class InferredAttributes {

  private final LiftedType liftedType;

  private InferredAttributes(LiftedType liftedType) {
    this.liftedType = liftedType;
  }

  public static InferredAttributes infer(
      TypeMirror originalReturnType,
      TypeTool tool) {
    LiftedType liftedType = LiftedType.lift(originalReturnType, tool);
    return new InferredAttributes(liftedType);
  }

  TypeMirror liftedType() {
    return liftedType.liftedType();
  }
}
