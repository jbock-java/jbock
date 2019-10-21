package net.jbock.coerce.collectorabsent.mapperabsent;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.ParameterSpec;
import net.jbock.coerce.BasicInfo;
import net.jbock.coerce.Coercion;
import net.jbock.coerce.ParameterType;
import net.jbock.coerce.collectorabsent.AbstractAttempt;
import net.jbock.coerce.mapper.MapperType;

import javax.lang.model.type.TypeMirror;
import java.util.Optional;
import java.util.function.Function;

class Attempt extends AbstractAttempt {

  Attempt(TypeMirror expectedReturnType, Function<ParameterSpec, CodeBlock> extractExpr, TypeMirror constructorParamType, ParameterType parameterType, BasicInfo basicInfo) {
    super(expectedReturnType, extractExpr, constructorParamType, parameterType, basicInfo);
  }

  Optional<Coercion> findCoercion() {
    return basicInfo().findMapExpr(expectedReturnType())
        .map(MapperType::create)
        .map(this::getCoercion);
  }
}
