package net.jbock.convert.matching;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.ParameterSpec;
import net.jbock.compiler.EnumName;
import net.jbock.convert.ParameterScope;
import net.jbock.convert.Skew;

import javax.inject.Inject;
import javax.lang.model.type.TypeMirror;
import java.util.Optional;

@ParameterScope
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
    return new Match(baseType, constructorParam, skew, enumName, Optional.empty());
  }

  public Match create(
      TypeMirror baseType,
      ParameterSpec constructorParam,
      Skew skew,
      CodeBlock extractExpr) {
    return new Match(baseType, constructorParam, skew, enumName, Optional.of(extractExpr));
  }
}
