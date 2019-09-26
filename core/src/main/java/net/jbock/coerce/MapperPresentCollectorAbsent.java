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

class MapperPresentCollectorAbsent {

  private final TypeElement mapperClass;
  private final BasicInfo basicInfo;
  private final List<Attempt> attempts = new ArrayList<>();

  MapperPresentCollectorAbsent(TypeElement mapperClass, BasicInfo basicInfo) {
    this.mapperClass = mapperClass;
    this.basicInfo = basicInfo;
    LiftedType liftedType = LiftedType.lift(basicInfo.originalReturnType(), tool());
    tool().unwrap(Optional.class, liftedType.liftedType()).ifPresent(wrapped ->
        attempts.add(new Attempt(wrapped, liftedType.extractExpr(), liftedType.liftedType(), false, true)));
    tool().unwrap(List.class, basicInfo.originalReturnType()).ifPresent(wrapped ->
        attempts.add(new Attempt(wrapped, p -> CodeBlock.of("$N", p), basicInfo.originalReturnType(), true, false)));
    attempts.add(new Attempt(liftedType.liftedType(), liftedType.extractExpr(), liftedType.liftedType(), false, false));
    attempts.add(new Attempt(basicInfo.originalReturnType(), p -> CodeBlock.of("$N", p), basicInfo.originalReturnType(), false, false));
  }

  private class Attempt {

    final TypeMirror expectedReturnType;
    final Function<ParameterSpec, CodeBlock> extractExpr;
    final TypeMirror constructorParamType;
    final boolean repeatable;
    final boolean optional;

    Attempt(TypeMirror expectedReturnType, Function<ParameterSpec, CodeBlock> extractExpr, TypeMirror constructorParamType, boolean repeatable, boolean optional) {
      this.expectedReturnType = expectedReturnType;
      this.extractExpr = extractExpr;
      this.constructorParamType = constructorParamType;
      this.repeatable = repeatable;
      this.optional = optional;
    }

    Either<Coercion, String> findCoercion() {
      Either<ReferenceMapperType, MapperClassAnalyzer.Failure> either = new MapperClassAnalyzer(basicInfo, expectedReturnType, mapperClass).checkReturnType();
      if (either instanceof Right) {
        return Either.right(((Right<ReferenceMapperType, MapperClassAnalyzer.Failure>) either).value().getMessage());
      }
      ReferenceMapperType mapperType = ((Left<ReferenceMapperType, MapperClassAnalyzer.Failure>) either).value();
      Optional<AbstractCollector> collector = repeatable ? Optional.of(new DefaultCollector(expectedReturnType)) : Optional.empty();
      return Either.left(Coercion.getCoercion(basicInfo, collector, mapperType, extractExpr, constructorParamType, optional));
    }
  }

  Coercion handleExplicitMapperNotRepeatable() {
    Either<Coercion, String> either = null;
    for (Attempt attempt : attempts) {
      either = attempt.findCoercion();
      if (either instanceof Left) {
        return ((Left<Coercion, String>) either).value();
      }
    }
    throw basicInfo.asValidationException(((Right<Coercion, String>) either).value());
  }

  private TypeTool tool() {
    return basicInfo.tool();
  }
}
