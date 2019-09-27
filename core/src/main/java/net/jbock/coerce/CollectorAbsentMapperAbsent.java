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

class CollectorAbsentMapperAbsent {

  private final BasicInfo basicInfo;

  CollectorAbsentMapperAbsent(BasicInfo basicInfo) {
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
      Optional<CodeBlock> autoMapper = basicInfo.findAutoMapper(expectedReturnType);
      if (!autoMapper.isPresent()) {
        return Optional.empty();
      }
      MapperType mapperType = MapperType.create(expectedReturnType, autoMapper.get());
      Optional<AbstractCollector> collector = parameterType.isRepeatable() ? Optional.of(new DefaultCollector(expectedReturnType)) : Optional.empty();
      return Optional.of(Coercion.getCoercion(basicInfo, collector, mapperType, extractExpr, constructorParamType, parameterType));
    }
  }

  private Attempt getAttempt() {
    TypeMirror returnType = basicInfo.originalReturnType();
    Optional<LiftedType> liftedType = LiftedType.unwrapOptional(returnType, tool());
    Optional<TypeMirror> wrappedInOptional = liftedType.map(LiftedType::liftedType)
        .flatMap(type -> tool().unwrap(Optional.class, type));
    Optional<TypeMirror> wrappedInList = tool().unwrap(List.class, returnType);
    if (wrappedInOptional.isPresent()) {
      return new Attempt(wrappedInOptional.get(), liftedType.get().extractExpr(), liftedType.get().liftedType(), OPTIONAL);
    }
    if (wrappedInList.isPresent()) {
      return new Attempt(wrappedInList.get(), p -> CodeBlock.of("$N", p), returnType, REPEATABLE);
    }
    return new Attempt(returnType, p -> CodeBlock.of("$N", p), returnType, REQUIRED);
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
