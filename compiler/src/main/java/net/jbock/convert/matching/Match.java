package net.jbock.convert.matching;

import com.squareup.javapoet.CodeBlock;
import net.jbock.convert.Mapped;
import net.jbock.model.Multiplicity;
import net.jbock.parameter.AbstractItem;

import javax.lang.model.type.TypeMirror;
import java.util.Optional;

public class Match {

  /* baseType ({List<A>, Optional<A>}) == A
   * baseType (OptionalInt) == Integer
   * baseType (int) == Integer
   */
  private final TypeMirror baseType;
  private final Optional<CodeBlock> extractExpr;
  private final Multiplicity skew;

  private Match(
      TypeMirror baseType,
      Multiplicity skew,
      Optional<CodeBlock> extractExpr) {
    this.baseType = baseType;
    this.skew = skew;
    this.extractExpr = extractExpr;
  }

  public static Match create(
      TypeMirror baseType,
      Multiplicity skew,
      CodeBlock extractExpr) {
    return new Match(baseType, skew, Optional.of(extractExpr));
  }

  public static Match create(
      TypeMirror baseType,
      Multiplicity skew) {
    return new Match(baseType, skew, Optional.empty());
  }

  public <P extends AbstractItem> Mapped<P> toConvertedParameter(
      CodeBlock mapExpr, P parameter) {
    return Mapped.create(mapExpr, extractExpr, skew, parameter);
  }

  public TypeMirror baseType() {
    return baseType;
  }

  public Multiplicity skew() {
    return skew;
  }
}
