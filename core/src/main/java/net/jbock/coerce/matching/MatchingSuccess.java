package net.jbock.coerce.matching;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.ParameterSpec;
import net.jbock.coerce.NonFlagSkew;

class MatchingSuccess {

  final CodeBlock mapExpr;
  final CodeBlock extractExpr;
  final ParameterSpec constructorParam;
  final NonFlagSkew skew;
//  final CodeBlock autoCollectExpr;

  MatchingSuccess(CodeBlock mapExpr, CodeBlock extractExpr, ParameterSpec constructorParam, NonFlagSkew skew) {
    this.mapExpr = mapExpr;
    this.extractExpr = extractExpr;
    this.constructorParam = constructorParam;
    this.skew = skew;
  }
}
