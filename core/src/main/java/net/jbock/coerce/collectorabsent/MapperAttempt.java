package net.jbock.coerce.collectorabsent;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.ParameterSpec;
import net.jbock.coerce.BasicInfo;
import net.jbock.coerce.Coercion;
import net.jbock.coerce.ParameterStyle;
import net.jbock.coerce.either.Either;

import javax.lang.model.type.TypeMirror;
import java.util.stream.Collectors;

abstract class MapperAttempt {

  private final CodeBlock extractExpr;
  private final ParameterSpec constructorParam;
  private final ParameterStyle style;
  private final TypeMirror testType;

  MapperAttempt(TypeMirror testType, CodeBlock extractExpr, ParameterSpec constructorParam, ParameterStyle style) {
    this.testType = testType;
    this.extractExpr = extractExpr;
    this.constructorParam = constructorParam;
    this.style = style;
  }

  Coercion getCoercion(BasicInfo basicInfo, CodeBlock mapExpr) {
    CodeBlock collectExpr = autoCollectExpr(basicInfo);
    return Coercion.getCoercion(basicInfo, collectExpr, mapExpr, extractExpr, style, constructorParam);
  }

  private CodeBlock autoCollectExpr(BasicInfo basicInfo) {
    return autoCollectExpr(basicInfo, style);
  }

  static CodeBlock autoCollectExpr(BasicInfo basicInfo, ParameterStyle style) {
    switch (style) {
      case OPTIONAL:
        return CodeBlock.of(".findAny()");
      case REQUIRED:
        return CodeBlock.of(".findAny().orElseThrow($T.$L::missingRequired)", basicInfo.optionType(),
            basicInfo.parameterName().enumConstant());
      case REPEATABLE:
        return CodeBlock.of(".collect($T.toList())", Collectors.class);
      default:
        throw new AssertionError("unexpected: " + style); // flags were handled earlier
    }
  }

  TypeMirror getTestType() {
    return testType;
  }

  abstract Either<String, Coercion> findCoercion(BasicInfo basicInfo);
}
