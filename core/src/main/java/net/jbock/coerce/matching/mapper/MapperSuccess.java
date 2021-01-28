package net.jbock.coerce.matching.mapper;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.ParameterSpec;
import net.jbock.coerce.Skew;
import net.jbock.coerce.matching.Match;
import net.jbock.coerce.matching.matcher.Matcher;

public class MapperSuccess {

  private final CodeBlock mapExpr;
  private final Match unwrapSuccess;
  private final Matcher matcher;

  MapperSuccess(
      CodeBlock mapExpr,
      Match unwrapSuccess,
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

  public Skew skew() {
    return unwrapSuccess.skew();
  }

  public CodeBlock tailExpr() {
    return matcher.tailExpr();
  }
}
