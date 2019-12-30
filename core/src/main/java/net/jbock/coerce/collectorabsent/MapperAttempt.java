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
import java.util.function.Function;

public abstract class MapperAttempt {

  private final Function<ParameterSpec, CodeBlock> extractExpr;
  private final TypeMirror constructorParamType;
  private final ParameterStyle style;
  private final TypeMirror expectedReturnType;
  private final BasicInfo basicInfo;

  protected MapperAttempt(TypeMirror expectedReturnType, Function<ParameterSpec, CodeBlock> extractExpr, TypeMirror constructorParamType, ParameterStyle style, BasicInfo basicInfo) {
    this.expectedReturnType = expectedReturnType;
    this.extractExpr = extractExpr;
    this.constructorParamType = constructorParamType;
    this.style = style;
    this.basicInfo = basicInfo;
  }

  protected Coercion getCoercion(MapperType mapperType) {
    if (style.isRepeatable()) {
      return Coercion.getCoercion(basicInfo, new DefaultCollector(expectedReturnType),
          mapperType, extractExpr, constructorParamType, style);
    }
    return Coercion.getCoercion(basicInfo, mapperType, extractExpr, constructorParamType, style);
  }

  protected TypeMirror expectedReturnType() {
    return expectedReturnType;
  }

  protected BasicInfo basicInfo() {
    return basicInfo;
  }

  protected abstract Either<String, Coercion> findCoercion();
}
