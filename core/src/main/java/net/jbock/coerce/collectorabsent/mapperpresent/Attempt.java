package net.jbock.coerce.collectorabsent.mapperpresent;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.ParameterSpec;
import net.jbock.coerce.BasicInfo;
import net.jbock.coerce.Coercion;
import net.jbock.coerce.ParameterType;
import net.jbock.coerce.collector.AbstractCollector;
import net.jbock.coerce.collector.DefaultCollector;
import net.jbock.coerce.collectorabsent.MapperClassAnalyzer;
import net.jbock.coerce.collectorabsent.MapperFailure;
import net.jbock.coerce.either.Either;
import net.jbock.coerce.mapper.ReferenceMapperType;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import java.util.Optional;
import java.util.function.Function;

class Attempt {

  private final TypeMirror expectedReturnType;
  private final Function<ParameterSpec, CodeBlock> extractExpr;
  private final TypeMirror constructorParamType;
  private final ParameterType parameterType;
  private final TypeElement mapperClass;
  private final BasicInfo basicInfo;

  Attempt(TypeMirror expectedReturnType, Function<ParameterSpec, CodeBlock> extractExpr, TypeMirror constructorParamType, ParameterType parameterType, TypeElement mapperClass, BasicInfo basicInfo) {
    this.expectedReturnType = expectedReturnType;
    this.extractExpr = extractExpr;
    this.constructorParamType = constructorParamType;
    this.parameterType = parameterType;
    this.mapperClass = mapperClass;
    this.basicInfo = basicInfo;
  }

  Either<String, Coercion> findCoercion() {
    Either<MapperFailure, ReferenceMapperType> either = new MapperClassAnalyzer(basicInfo, expectedReturnType, mapperClass)
        .checkReturnType();
    return either.map(MapperFailure::getMessage,
        mapperType -> Coercion.getCoercion(basicInfo, collector(),
            mapperType, extractExpr, constructorParamType, parameterType));
  }

  private Optional<AbstractCollector> collector() {
    return parameterType.isRepeatable() ? Optional.of(new DefaultCollector(expectedReturnType)) : Optional.empty();
  }
}
