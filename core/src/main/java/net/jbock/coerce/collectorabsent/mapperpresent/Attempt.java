package net.jbock.coerce.collectorabsent.mapperpresent;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.ParameterSpec;
import net.jbock.coerce.BasicInfo;
import net.jbock.coerce.Coercion;
import net.jbock.coerce.MapperClassValidator;
import net.jbock.coerce.ParameterType;
import net.jbock.coerce.collectorabsent.AbstractAttempt;
import net.jbock.coerce.either.Either;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import java.util.function.Function;

class Attempt extends AbstractAttempt {

  private final TypeElement mapperClass;

  Attempt(TypeMirror expectedReturnType, Function<ParameterSpec, CodeBlock> extractExpr, TypeMirror constructorParamType, ParameterType parameterType, TypeElement mapperClass, BasicInfo basicInfo) {
    super(expectedReturnType, extractExpr, constructorParamType, parameterType, basicInfo);
    this.mapperClass = mapperClass;
  }

  Either<String, Coercion> findCoercion() {
    return new MapperClassValidator(basicInfo(), expectedReturnType(), mapperClass)
        .checkReturnType()
        .map(this::getCoercion);
  }
}
