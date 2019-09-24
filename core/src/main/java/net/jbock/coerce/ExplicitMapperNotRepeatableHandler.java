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
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

class ExplicitMapperNotRepeatableHandler {

  private final TypeElement mapperClass;
  private final BasicInfo basicInfo;

  ExplicitMapperNotRepeatableHandler(TypeElement mapperClass, BasicInfo basicInfo) {
    this.mapperClass = mapperClass;
    this.basicInfo = basicInfo;
  }

  // TODO refactoring
  Coercion handleExplicitMapperNotRepeatable() {
    Function<ParameterSpec, CodeBlock> extractExpr;
    ReferenceMapperType mapperType;
    TypeMirror constructorParamType;
    Optional<AbstractCollector> collector;
    Either<ReferenceMapperType, MapperClassAnalyzer.Failure> either = new MapperClassAnalyzer(basicInfo, basicInfo.originalReturnType(), mapperClass).checkReturnType();
    if (either instanceof Left) {
      mapperType = getLeft(either);
      extractExpr = p -> CodeBlock.of("$N", p);
      constructorParamType = basicInfo.originalReturnType();
      collector = Optional.empty();
      return createCoercion(extractExpr, mapperType, constructorParamType, collector);
    }
    LiftedType liftedType = LiftedType.lift(basicInfo.originalReturnType(), tool());
    Optional<TypeMirror> optionalInfo = tool().unwrap(Optional.class, liftedType.liftedType());
    if (optionalInfo.isPresent()) {
      either = new MapperClassAnalyzer(basicInfo, optionalInfo.get(), mapperClass).checkReturnType();
      if (either instanceof Left) {
        mapperType = getLeft(either).asOptional();
        extractExpr = liftedType.extractExpr();
        constructorParamType = liftedType.liftedType();
        collector = Optional.empty();
        return createCoercion(extractExpr, mapperType, constructorParamType, collector);
      }
    }
    either = new MapperClassAnalyzer(basicInfo, liftedType.liftedType(), mapperClass).checkReturnType();
    if (either instanceof Left) {
      mapperType = getLeft(either);
      extractExpr = liftedType.extractExpr();
      constructorParamType = liftedType.liftedType();
      collector = Optional.empty();
      return createCoercion(extractExpr, mapperType, constructorParamType, collector);
    }
    Optional<TypeMirror> wrappedType = tool().unwrap(List.class, basicInfo.originalReturnType());
    if (!wrappedType.isPresent()) {
      throw ((Right<ReferenceMapperType, MapperClassAnalyzer.Failure>) either).value().boom(basicInfo);
    }
    either = new MapperClassAnalyzer(basicInfo, wrappedType.get(), mapperClass).checkReturnType();
    if (either instanceof Left) {
      mapperType = getLeft(either);
      extractExpr = p -> CodeBlock.of("$N", p);
      constructorParamType = basicInfo.returnType();
      collector = Optional.of(new DefaultCollector(wrappedType.get()));
      return createCoercion(extractExpr, mapperType, constructorParamType, collector);
    }
    throw ((Right<ReferenceMapperType, MapperClassAnalyzer.Failure>) either).value().boom(basicInfo);
  }

  private ReferenceMapperType getLeft(Either<ReferenceMapperType, MapperClassAnalyzer.Failure> either) {
    return ((Left<ReferenceMapperType, MapperClassAnalyzer.Failure>) either).value();
  }

  private Coercion createCoercion(Function<ParameterSpec, CodeBlock> extractExpr, ReferenceMapperType mapperType, TypeMirror constructorParamType, Optional<AbstractCollector> collector) {
    return Coercion.getCoercion(basicInfo, collector, mapperType, extractExpr, constructorParamType);
  }

  private TypeTool tool() {
    return basicInfo.tool();
  }
}
