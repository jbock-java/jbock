package net.jbock.coerce.matching.mapper;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.ParameterSpec;
import net.jbock.coerce.Skew;
import net.jbock.coerce.matching.UnwrapSuccess;
import net.jbock.coerce.matching.matcher.Matcher;

public class MapperSuccess {

  private final CodeBlock mapExpr;
  private final UnwrapSuccess unwrapSuccess;
  private final Matcher matcher;

  MapperSuccess(
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

  public Skew skew() {
    return matcher.skew();
  }

  public CodeBlock tailExpr() {
    return matcher.tailExpr();
  }
}
