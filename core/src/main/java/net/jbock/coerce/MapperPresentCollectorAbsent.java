package net.jbock.coerce;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.ParameterSpec;
import net.jbock.coerce.collector.AbstractCollector;
import net.jbock.coerce.collector.DefaultCollector;
import net.jbock.coerce.either.Either;
import net.jbock.coerce.either.Left;
import net.jbock.coerce.either.Right;
import net.jbock.coerce.mapper.ReferenceMapperType;
import net.jbock.compiler.TypeTool;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static net.jbock.coerce.ParameterType.OPTIONAL;
import static net.jbock.coerce.ParameterType.REPEATABLE;
import static net.jbock.coerce.ParameterType.REQUIRED;

class MapperPresentCollectorAbsent {

  private final TypeElement mapperClass;
  private final BasicInfo basicInfo;

  MapperPresentCollectorAbsent(BasicInfo basicInfo, TypeElement mapperClass) {
    this.mapperClass = mapperClass;
    this.basicInfo = basicInfo;
  }

  private List<Attempt> getAttempts() {
    List<Attempt> attempts = new ArrayList<>();
    LiftedType liftedType = LiftedType.lift(basicInfo.originalReturnType(), tool());
    tool().unwrap(Optional.class, liftedType.liftedType()).ifPresent(wrapped ->
        attempts.add(new Attempt(wrapped, liftedType.extractExpr(), liftedType.liftedType(), OPTIONAL)));
    tool().unwrap(List.class, basicInfo.originalReturnType()).ifPresent(wrapped ->
        attempts.add(new Attempt(wrapped, p -> CodeBlock.of("$N", p), basicInfo.originalReturnType(), REPEATABLE)));
    if (!tool().isSameType(liftedType.liftedType(), basicInfo.originalReturnType())) {
      attempts.add(new Attempt(liftedType.liftedType(), liftedType.extractExpr(), liftedType.liftedType(), REQUIRED));
    }
    attempts.add(new Attempt(basicInfo.originalReturnType(), p -> CodeBlock.of("$N", p), basicInfo.originalReturnType(), REQUIRED));
    return attempts;
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

    Either<Coercion, String> findCoercion() {
      Either<ReferenceMapperType, MapperClassAnalyzer.Failure> either = new MapperClassAnalyzer(basicInfo, expectedReturnType, mapperClass).checkReturnType();
      if (either instanceof Right) {
        return Either.right(((Right<ReferenceMapperType, MapperClassAnalyzer.Failure>) either).value().getMessage());
      }
      ReferenceMapperType mapperType = ((Left<ReferenceMapperType, MapperClassAnalyzer.Failure>) either).value();
      Optional<AbstractCollector> collector = parameterType.repeatable() ? Optional.of(new DefaultCollector(expectedReturnType)) : Optional.empty();
      return Either.left(Coercion.getCoercion(basicInfo, collector, mapperType, extractExpr, constructorParamType, parameterType));
    }
  }

  Coercion findCoercion() {
    List<Attempt> attempts = getAttempts();
    Either<Coercion, String> either = null;
    for (Attempt attempt : attempts) {
      either = attempt.findCoercion();
      if (either instanceof Left) {
        return ((Left<Coercion, String>) either).value();
      }
    }
    if (either == null) {
      throw new AssertionError();
    }
    throw basicInfo.asValidationException(((Right<Coercion, String>) either).value());
  }

  private TypeTool tool() {
    return basicInfo.tool();
  }
}
