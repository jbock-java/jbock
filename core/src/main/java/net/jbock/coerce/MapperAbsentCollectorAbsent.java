package net.jbock.coerce;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.ParameterSpec;
import net.jbock.coerce.collector.AbstractCollector;
import net.jbock.coerce.collector.DefaultCollector;
import net.jbock.coerce.mapper.MapperType;
import net.jbock.compiler.TypeTool;

import javax.lang.model.type.TypeMirror;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static net.jbock.coerce.ParameterType.OPTIONAL;
import static net.jbock.coerce.ParameterType.REPEATABLE;
import static net.jbock.coerce.ParameterType.REQUIRED;

class MapperAbsentCollectorAbsent {

  private final BasicInfo basicInfo;

  MapperAbsentCollectorAbsent(BasicInfo basicInfo) {
    this.basicInfo = basicInfo;
  }

  private class Attempt {

    final TypeMirror expectedReturnType;
    final Function<ParameterSpec, CodeBlock> extractExpr;
    final TypeMirror constructorParamType;
    final ParameterType parameterType;

    Attempt(TypeMirror expectedReturnType, Function<ParameterSpec, CodeBlock> extractExpr, TypeMirror constructorParamType, ParameterType parameterType) {
      this.expectedReturnType = expectedReturnType;
      this.extractExpr = extractExpr;
      this.constructorParamType = constructorParamType;
      this.parameterType = parameterType;
    }

    Optional<Coercion> findCoercion() {
      Optional<CodeBlock> autoMapper = CoercionProvider.findAutoMapper(expectedReturnType, basicInfo);
      if (!autoMapper.isPresent()) {
        return Optional.empty();
      }
      MapperType mapperType = MapperType.create(expectedReturnType, autoMapper.get());
      Optional<AbstractCollector> collector = parameterType.repeatable() ? Optional.of(new DefaultCollector(expectedReturnType)) : Optional.empty();
      return Optional.of(Coercion.getCoercion(basicInfo, collector, mapperType, extractExpr, constructorParamType, parameterType));
    }
  }

  private Attempt getAttempt() {
    LiftedType liftedType = LiftedType.lift(basicInfo.originalReturnType(), tool());
    Optional<TypeMirror> optionalInfo = tool().unwrap(Optional.class, liftedType.liftedType());
    if (optionalInfo.isPresent()) {
      return new Attempt(optionalInfo.get(), liftedType.extractExpr(), liftedType.liftedType(), OPTIONAL);
    }
    Optional<TypeMirror> listInfo = tool().unwrap(List.class, basicInfo.originalReturnType());
    if (listInfo.isPresent()) {
      return new Attempt(listInfo.get(), p -> CodeBlock.of("$N", p), basicInfo.originalReturnType(), REPEATABLE);
    }
    return new Attempt(basicInfo.originalReturnType(), p -> CodeBlock.of("$N", p), basicInfo.originalReturnType(), REQUIRED);
  }

  Coercion findCoercion() {
    Attempt attempt = getAttempt();
    return attempt.findCoercion()
        .orElseThrow(() -> basicInfo.asValidationException("Unknown parameter type. Try defining a custom mapper or collector."));
  }

  private TypeTool tool() {
    return basicInfo.tool();
  }
}
