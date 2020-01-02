package net.jbock.coerce.collectorabsent;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.ParameterSpec;
import net.jbock.coerce.BasicInfo;
import net.jbock.coerce.Coercion;
import net.jbock.coerce.ParameterStyle;
import net.jbock.coerce.collectors.DefaultCollector;
import net.jbock.coerce.either.Either;
import net.jbock.coerce.mapper.MapperType;

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

  protected Coercion getCoercion(BasicInfo basicInfo, MapperType mapperType) {
    if (style.isRepeatable()) {
      return Coercion.getCoercion(basicInfo, new DefaultCollector(testType),
          mapperType, extractExpr, constructorParam, style);
    }
    return Coercion.getCoercion(basicInfo, CodeBlock.builder().build(), mapperType, extractExpr, style, constructorParam);
  }

  protected TypeMirror getTestType() {
    return testType;
  }

  public abstract Either<String, Coercion> findCoercion(BasicInfo basicInfo);
}
