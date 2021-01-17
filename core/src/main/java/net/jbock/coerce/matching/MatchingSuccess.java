package net.jbock.coerce.matching;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.ParameterSpec;
import net.jbock.coerce.NonFlagSkew;

class MatchingSuccess {

  private final CodeBlock mapExpr;
  private final UnwrapSuccess unwrapSuccess;
  private final Matcher matcher;

  MatchingSuccess(
      CodeBlock mapExpr,
      UnwrapSuccess unwrapSuccess,
      Matcher matcher) {
    this.mapExpr = mapExpr;
    this.unwrapSuccess = unwrapSuccess;
    this.matcher = matcher;
  }

  public CodeBlock mapExpr() {
    return mapExpr;
  }

  public CodeBlock extractExpr() {
    return unwrapSuccess.extractExpr();
  }

  public ParameterSpec constructorParam() {
    return unwrapSuccess.constructorParam();
  }

  public NonFlagSkew skew() {
    return matcher.skew();
  }

  public CodeBlock tailExpr() {
    return matcher.tailExpr();
  }
}
