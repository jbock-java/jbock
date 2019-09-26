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

// TODO refactoring
class MapperAbsentCollectorAbsent {

  private final BasicInfo basicInfo;

  MapperAbsentCollectorAbsent(BasicInfo basicInfo) {
    this.basicInfo = basicInfo;
  }

  Coercion findCoercion() {
    Optional<CodeBlock> mapExpr = findAutoMapper(tool().box(basicInfo.originalReturnType()));
    Function<ParameterSpec, CodeBlock> extractExpr;
    Optional<TypeMirror> listInfo = tool().unwrap(List.class, basicInfo.originalReturnType());
    Optional<AbstractCollector> collector;
    LiftedType liftedType = LiftedType.lift(basicInfo.originalReturnType(), tool());
    Optional<TypeMirror> optionalInfo = tool().unwrap(Optional.class, liftedType.liftedType());
    boolean optional = false;
    MapperType mapperType = null;
    if (optionalInfo.isPresent()) {
      mapExpr = findAutoMapper(optionalInfo.get());
      extractExpr = liftedType.extractExpr();
      if (mapExpr.isPresent()) {
        mapperType = MapperType.create(optionalInfo.get(), mapExpr.get());
        optional = true;
      }
      collector = Optional.empty();
    } else if (listInfo.isPresent()) {
      mapExpr = findAutoMapper(listInfo.get());
      extractExpr = p -> CodeBlock.of("$N", p);
      if (mapExpr.isPresent()) {
        mapperType = MapperType.create(listInfo.get(), mapExpr.get());
      }
      collector = Optional.of(new DefaultCollector(listInfo.get()));
    } else {
      collector = Optional.empty();
      if (mapExpr.isPresent()) {
        mapperType = MapperType.create(tool().box(basicInfo.originalReturnType()), mapExpr.get());
      }
      extractExpr = p -> CodeBlock.of("$N", p);
    }
    if (mapperType == null) {
      throw basicInfo.asValidationException("Unknown parameter type. Try defining a custom mapper or collector.");
    }
    TypeMirror constructorParamType = liftedType.liftedType();
    return Coercion.getCoercion(basicInfo, collector, mapperType, extractExpr, constructorParamType, optional);
  }

  private Optional<CodeBlock> findAutoMapper(TypeMirror innerType) {
    return CoercionProvider.findAutoMapper(innerType, basicInfo);
  }

  private TypeTool tool() {
    return basicInfo.tool();
  }
}
