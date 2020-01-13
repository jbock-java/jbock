package net.jbock.coerce.collectorabsent.auto;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.ParameterSpec;
import net.jbock.coerce.BasicInfo;
import net.jbock.coerce.Coercion;
import net.jbock.coerce.ParameterStyle;
import net.jbock.coerce.collectorabsent.MapperAttempt;
import net.jbock.coerce.either.Either;
import net.jbock.coerce.mapper.MapperType;

import javax.lang.model.type.TypeMirror;

class AutoAttempt extends MapperAttempt {

  AutoAttempt(TypeMirror test, CodeBlock extractExpr, ParameterSpec constructorParam, ParameterStyle style) {
    super(test, extractExpr, constructorParam, style);
  }

  AutoAttempt(TypeMirror test, ParameterSpec constructorParam, ParameterStyle style) {
    this(test, CodeBlock.of("$N", constructorParam), constructorParam, style);
  }

  @Override
  public Either<String, Coercion> findCoercion(BasicInfo basicInfo) {
    return basicInfo.findAutoMapper(getTestType())
        .map(MapperType::new)
        .map(mapperType -> getCoercion(basicInfo, mapperType))
        .<Either<String, Coercion>>map(Either::right)
        .orElseGet(() -> Either.left(String.format("Unknown parameter type: %s. Try defining a custom mapper or collector.",
            basicInfo.originalReturnType())));
  }
}
