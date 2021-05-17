package net.jbock.convert.matching;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.ParameterSpec;
import dagger.Reusable;
import net.jbock.compiler.EnumName;
import net.jbock.convert.Skew;

import javax.inject.Inject;
import javax.lang.model.type.TypeMirror;

@Reusable
public class MatchFactory {

  private final EnumName enumName;

  @Inject
  MatchFactory(EnumName enumName) {
    this.enumName = enumName;
  }

  public Match create(
      TypeMirror baseType,
      ParameterSpec constructorParam,
      Skew skew) {
    return create(baseType, constructorParam, skew, CodeBlock.of("$N", constructorParam));
  }

  public Match create(
      TypeMirror baseType,
      ParameterSpec constructorParam,
      Skew skew,
      CodeBlock extractExpr) {
    return new Match(baseType, constructorParam, extractExpr, skew, enumName);
  }
}
