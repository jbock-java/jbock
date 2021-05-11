package net.jbock.convert.matching;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.ParameterSpec;
import net.jbock.compiler.EnumName;
import net.jbock.convert.Skew;

import javax.inject.Inject;
import javax.lang.model.type.TypeMirror;

public class MatchFactory {

  private final EnumName enumName;

  @Inject
  MatchFactory(EnumName enumName) {
    this.enumName = enumName;
  }

  public Match create(
      TypeMirror wrappedType,
      ParameterSpec constructorParam,
      Skew skew) {
    return create(wrappedType, constructorParam, skew, CodeBlock.of("$N", constructorParam));
  }

  public Match create(
      TypeMirror wrappedType,
      ParameterSpec constructorParam,
      Skew skew,
      CodeBlock extractExpr) {
    return new Match(wrappedType, constructorParam, extractExpr, skew, enumName);
  }
}
