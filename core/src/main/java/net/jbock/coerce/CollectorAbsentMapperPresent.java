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

class CollectorAbsentMapperPresent {

  private final TypeElement mapperClass;
  private final BasicInfo basicInfo;

  CollectorAbsentMapperPresent(BasicInfo basicInfo, TypeElement mapperClass) {
    this.mapperClass = mapperClass;
    this.basicInfo = basicInfo;
  }

  private List<Attempt> getAttempts() {
    TypeMirror returnType = basicInfo.originalReturnType();
    Optional<CanonicalOptional> canonicalOptional = CanonicalOptional.unwrap(returnType, tool());
    Optional<TypeMirror> list = tool().unwrap(List.class, returnType);
    List<Attempt> attempts = new ArrayList<>();
    canonicalOptional.ifPresent(optional ->
        attempts.add(new Attempt(optional.wrapped(), optional.extractExpr(), optional.canonicalType(), OPTIONAL)));
    list.ifPresent(wrapped ->
        attempts.add(new Attempt(wrapped, p -> CodeBlock.of("$N", p), returnType, REPEATABLE)));
    canonicalOptional.ifPresent(type ->
        attempts.add(new Attempt(type.canonicalType(), type.extractExpr(), type.canonicalType(), REQUIRED)));
    attempts.add(new Attempt(tool().box(returnType), p -> CodeBlock.of("$N", p), returnType, REQUIRED));
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
      Optional<AbstractCollector> collector = parameterType.isRepeatable() ? Optional.of(new DefaultCollector(expectedReturnType)) : Optional.empty();
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
    if (either == null) { // impossible: there is always at least one attempt
      throw new AssertionError();
    }
    String message = ((Right<Coercion, String>) either).value();
    throw basicInfo.asValidationException(message);
  }

  private TypeTool tool() {
    return basicInfo.tool();
  }
}
