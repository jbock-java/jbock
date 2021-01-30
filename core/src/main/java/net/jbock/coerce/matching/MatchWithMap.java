package net.jbock.coerce.matching;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.ParameterSpec;
import net.jbock.coerce.Skew;
import net.jbock.coerce.matching.Match;

public class MatchWithMap {

  private final CodeBlock mapExpr;
  private final Match match;

  public MatchWithMap(CodeBlock mapExpr, Match match) {
    this.mapExpr = mapExpr;
    this.match = match;
  }

  public CodeBlock mapExpr() {
    return mapExpr;
  }

  public CodeBlock extractExpr() {
    return match.extractExpr();
  }

  public ParameterSpec constructorParam() {
    return match.constructorParam();
  }

  public Skew skew() {
    return match.skew();
  }

  public CodeBlock tailExpr() {
    return match.tailExpr();
  }
}
