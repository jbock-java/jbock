package net.jbock.coerce;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import net.jbock.coerce.collector.AbstractCollector;
import net.jbock.coerce.collector.DefaultCollector;
import net.jbock.coerce.mapper.MapperType;
import net.jbock.coerce.mapper.ReferenceMapperType;
import net.jbock.compiler.ParamName;
import net.jbock.compiler.TypeTool;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static javax.lang.model.element.Modifier.FINAL;
import static net.jbock.coerce.ParameterType.FLAG;
import static net.jbock.coerce.ParameterType.REPEATABLE;

public class CoercionProvider {

  private final BasicInfo basicInfo;

  private CoercionProvider(BasicInfo basicInfo) {
    this.basicInfo = basicInfo;
  }

  public static Coercion flagCoercion(ExecutableElement sourceMethod, ParamName paramName) {
    ParameterSpec name = ParameterSpec.builder(TypeName.get(sourceMethod.getReturnType()), paramName.snake()).build();
    return new Coercion(
        Optional.empty(),
        CodeBlock.of(""),
        name,
        FieldSpec.builder(TypeName.get(sourceMethod.getReturnType()), paramName.snake(), FINAL).build(),
        CodeBlock.of("$N", name),
        FLAG);
  }

  public static Coercion findCoercion(
      ExecutableElement sourceMethod,
      ParamName paramName,
      TypeElement mapperClass,
      TypeElement collectorClass,
      InferredAttributes attributes,
      TypeTool tool) {
    BasicInfo basicInfo = BasicInfo.create(
        mapperClass, collectorClass,
        attributes, paramName, sourceMethod, tool);
    CoercionProvider coercionProvider = new CoercionProvider(basicInfo);
    return coercionProvider.run();
  }

  private Coercion run() {
    if (basicInfo.collectorClass().isPresent()) {
      return handleRepeatable();
    } else {
      return handleNotRepeatable();
    }
  }

  private Coercion handleNotRepeatable() {
    if (basicInfo.mapperClass().isPresent()) {
      return new MapperPresentCollectorAbsent(basicInfo, basicInfo.mapperClass().get()).findCoercion();
    } else {
      return new MapperAbsentCollectorAbsent(basicInfo).findCoercion();
    }
  }

  private Coercion handleRepeatable() {
    if (basicInfo.mapperClass().isPresent()) {
      return handleRepeatableExplicitMapper(basicInfo.mapperClass().get());
    } else {
      return handleRepeatableAutoMapper();
    }
  }


  private Coercion handleRepeatableAutoMapper() {
    AbstractCollector collectorInfo = collectorInfo();
    CodeBlock mapExpr = findAutoMapper(collectorInfo.inputType())
        .orElseThrow(() -> basicInfo.asValidationException("Unknown parameter type. Define a custom mapper."));
    MapperType mapperType = MapperType.create(collectorInfo.inputType(), mapExpr);
    Function<ParameterSpec, CodeBlock> extractExpr = p -> CodeBlock.of("$N", p);
    TypeMirror constructorParamType = basicInfo.originalReturnType();
    return Coercion.getCoercion(basicInfo, Optional.of(collectorInfo), mapperType, extractExpr, constructorParamType, REPEATABLE);
  }

  private Coercion handleRepeatableExplicitMapper(TypeElement mapperClass) {
    AbstractCollector collectorInfo = collectorInfo();
    ReferenceMapperType mapperType = new MapperClassValidator(basicInfo, collectorInfo.inputType(), mapperClass).checkReturnType();
    Function<ParameterSpec, CodeBlock> extractExpr = p -> CodeBlock.of("$N", p);
    TypeMirror constructorParamType = basicInfo.originalReturnType();
    return Coercion.getCoercion(basicInfo, Optional.of(collectorInfo), mapperType, extractExpr, constructorParamType, REPEATABLE);
  }

  private Optional<CodeBlock> findAutoMapper(TypeMirror innerType) {
    return findAutoMapper(innerType, basicInfo);
  }

  static Optional<CodeBlock> findAutoMapper(TypeMirror innerType, BasicInfo basicInfo) {
    Optional<CodeBlock> mapExpr = AutoMapper.findAutoMapper(basicInfo.tool(), basicInfo.tool().box(innerType));
    if (mapExpr.isPresent()) {
      return mapExpr;
    }
    if (isEnumType(innerType, basicInfo)) {
      return Optional.of(CodeBlock.of("$T::valueOf", innerType));
    }
    return Optional.empty();
  }

  private static boolean isEnumType(TypeMirror mirror, BasicInfo basicInfo) {
    List<? extends TypeMirror> supertypes = basicInfo.tool().getDirectSupertypes(mirror);
    if (supertypes.isEmpty()) {
      // not an enum
      return false;
    }
    TypeMirror superclass = supertypes.get(0);
    if (!basicInfo.tool().isSameErasure(superclass, Enum.class)) {
      // not an enum
      return false;
    }
    if (basicInfo.tool().isPrivateType(mirror)) {
      throw basicInfo.asValidationException("The enum may not be private.");
    }
    return true;
  }

  private AbstractCollector collectorInfo() {
    if (basicInfo.collectorClass().isPresent()) {
      return new CollectorClassValidator(basicInfo, basicInfo.collectorClass().get()).getCollectorInfo();
    }
    Optional<TypeMirror> wrapped = tool().unwrap(List.class, basicInfo.originalReturnType());
    if (!wrapped.isPresent()) {
      throw basicInfo.asValidationException("Either define a custom collector, or return List.");
    }
    return new DefaultCollector(wrapped.get());
  }

  private TypeTool tool() {
    return basicInfo.tool();
  }
}
