package net.jbock.coerce.collectorabsent;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.ParameterSpec;
import net.jbock.coerce.BasicInfo;
import net.jbock.coerce.Coercion;
import net.jbock.coerce.ParameterStyle;
import net.jbock.coerce.collectors.CollectorInfo;
import net.jbock.coerce.either.Either;

import javax.lang.model.type.TypeMirror;

public abstract class MapperAttempt {

  private final CodeBlock extractExpr;
  private final ParameterSpec constructorParam;
  private final ParameterStyle style;
  private final TypeMirror testType;

  protected MapperAttempt(TypeMirror testType, CodeBlock extractExpr, ParameterSpec constructorParam, ParameterStyle style) {
    this.testType = testType;
    this.extractExpr = extractExpr;
    this.constructorParam = constructorParam;
    this.style = style;
  }

  protected Coercion getCoercion(BasicInfo basicInfo, CodeBlock mapExpr) {
    CodeBlock collectExpr = getCollectExpr(basicInfo);
    return Coercion.getCoercion(basicInfo, collectExpr, mapExpr, extractExpr, style, constructorParam);
  }

  private CodeBlock getCollectExpr(BasicInfo basicInfo) {
    switch (style) {
      case OPTIONAL:
        return CodeBlock.of(".findAny()");
      case REQUIRED:
        return CodeBlock.of(".findAny().orElseThrow($T.$L::missingRequired)", basicInfo.optionType(),
            basicInfo.parameterName().enumConstant());
      case REPEATABLE:
        return CollectorInfo.createDefault(testType).collectExpr();
      default:
        throw new AssertionError("unexpected: " + style);
    }
  }

  protected TypeMirror getTestType() {
    return testType;
  }

  public abstract Either<String, Coercion> findCoercion(BasicInfo basicInfo);
}
