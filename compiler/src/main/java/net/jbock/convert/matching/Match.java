package net.jbock.convert.matching;

import com.squareup.javapoet.CodeBlock;
import net.jbock.convert.ConvertedParameter;
import net.jbock.convert.Skew;
import net.jbock.parameter.AbstractParameter;

import javax.lang.model.type.TypeMirror;
import java.util.Optional;

public class Match {

  /* baseType ({List<A>, Optional<A>}) == A
   * baseType (OptionalInt) == int
   */
  private final TypeMirror baseType;
  private final Optional<CodeBlock> extractExpr;
  private final Skew skew;

  private Match(
      TypeMirror baseType,
      Skew skew,
      Optional<CodeBlock> extractExpr) {
    this.baseType = baseType;
    this.skew = skew;
    this.extractExpr = extractExpr;
  }

  public static Match create(
      TypeMirror baseType,
      Skew skew,
      CodeBlock extractExpr) {
    return new Match(baseType, skew, Optional.of(extractExpr));
  }

  public static Match create(
      TypeMirror baseType,
      Skew skew) {
    return new Match(baseType, skew, Optional.empty());
  }

  public <P extends AbstractParameter> ConvertedParameter<P> toConvertedParameter(
      Optional<CodeBlock> mapExpr, P parameter) {
    return ConvertedParameter.create(mapExpr, extractExpr, skew, parameter);
  }

  public TypeMirror baseType() {
    return baseType;
  }

  public Skew skew() {
    return skew;
  }
}
