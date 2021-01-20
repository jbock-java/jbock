package net.jbock.coerce.matching.mapper;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.ParameterSpec;
import net.jbock.coerce.Skew;
import net.jbock.coerce.matching.UnwrapSuccess;
import net.jbock.coerce.matching.matcher.Matcher;

class MapperSuccess {

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

  CodeBlock mapExpr() {
    return mapExpr;
  }

  CodeBlock extractExpr() {
    return unwrapSuccess.extractExpr();
  }

  ParameterSpec constructorParam() {
    return unwrapSuccess.constructorParam();
  }

  Skew skew() {
    return matcher.skew();
  }

  CodeBlock tailExpr() {
    return matcher.tailExpr();
  }
}
