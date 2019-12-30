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
import java.util.function.Function;

class AutoAttempt extends MapperAttempt {

  AutoAttempt(TypeMirror expectedReturnType, Function<ParameterSpec, CodeBlock> extractExpr, TypeMirror constructorParamType, ParameterStyle style, BasicInfo basicInfo) {
    super(expectedReturnType, extractExpr, constructorParamType, style, basicInfo);
  }

  @Override
  protected Either<String, Coercion> findCoercion() {
    return basicInfo().findAutoMapper(expectedReturnType())
        .map(MapperType::create)
        .map(this::getCoercion)
        .<Either<String, Coercion>>map(Either::right)
        .orElseGet(() -> Either.left(String.format("Unknown parameter type: %s. Try defining a custom mapper or collector.",
            basicInfo().originalReturnType())));
  }
}
