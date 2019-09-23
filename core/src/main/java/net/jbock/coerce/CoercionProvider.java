package net.jbock.coerce;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import net.jbock.coerce.coercions.CoercionFactory;
import net.jbock.coerce.coercions.EnumCoercion;
import net.jbock.coerce.coercions.ExplicitMapperCoercion;
import net.jbock.coerce.coercions.StandardCoercions;
import net.jbock.coerce.collector.AbstractCollector;
import net.jbock.coerce.collector.DefaultCollector;
import net.jbock.coerce.mapper.MapperType;
import net.jbock.coerce.mapper.ReferenceMapperType;
import net.jbock.compiler.ParamName;
import net.jbock.compiler.TypeTool;
import net.jbock.compiler.ValidationException;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static javax.lang.model.element.Modifier.FINAL;

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
        true);
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
      return handleExplicitMapperNotRepeatable(basicInfo.mapperClass().get());
    } else {
      return handleAutoMapperNotRepeatable();
    }
  }

  private Coercion handleRepeatable() {
    if (basicInfo.mapperClass().isPresent()) {
      return handleRepeatableExplicitMapper(basicInfo.mapperClass().get());
    } else {
      return handleRepeatableAutoMapper();
    }
  }

  // TODO refactoring
  private Coercion handleAutoMapperNotRepeatable() {
    Optional<CoercionFactory> factory = findCoercion(tool().box(basicInfo.originalReturnType()));
    Function<ParameterSpec, CodeBlock> extractExpr;
    Optional<TypeMirror> listInfo = tool().unwrap(List.class, basicInfo.originalReturnType());
    Optional<AbstractCollector> collector;
    Optional<TypeMirror> optionalInfo = tool().liftingUnwrap(basicInfo.originalReturnType());
    MapperType mapperType = null;
    if (optionalInfo.isPresent()) {
      factory = findCoercion(optionalInfo.get());
      extractExpr = LiftedType.lift(basicInfo.originalReturnType(), tool()).extractExpr();
      if (factory.isPresent()) {
        mapperType = MapperType.create(optionalInfo.get(), factory.get().createMapper(optionalInfo.get()), true);
      }
      collector = Optional.empty();
    } else if (listInfo.isPresent()) {
      factory = findCoercion(listInfo.get());
      extractExpr = p -> CodeBlock.of("$N", p);
      if (factory.isPresent()) {
        mapperType = MapperType.create(listInfo.get(), factory.get().createMapper(listInfo.get()), false);
      }
      collector = Optional.of(new DefaultCollector(listInfo.get()));
    } else {
      collector = Optional.empty();
      if (factory.isPresent()) {
        mapperType = MapperType.create(tool().box(basicInfo.originalReturnType()), factory.get().createMapper(tool().box(basicInfo.originalReturnType())), false);
      }
      extractExpr = p -> CodeBlock.of("$N", p);
    }
    if (mapperType == null) {
      throw basicInfo.asValidationException("Unknown parameter type. Define a custom mapper.");
    }
    TypeMirror constructorParamType = LiftedType.lift(basicInfo.originalReturnType(), tool()).liftedType();
    return Coercion.getCoercion(factory.get(), basicInfo, collector, mapperType, extractExpr, constructorParamType);
  }

  // TODO refactoring
  private Coercion handleExplicitMapperNotRepeatable(TypeElement mapperClass) {
    Function<ParameterSpec, CodeBlock> extractExpr;
    ReferenceMapperType mapperType;
    TypeMirror constructorParamType;
    Optional<AbstractCollector> collector;
    try {
      mapperType = new MapperClassAnalyzer(basicInfo, basicInfo.originalReturnType(), mapperClass).checkReturnType();
      extractExpr = p -> CodeBlock.of("$N", p);
      constructorParamType = basicInfo.originalReturnType();
      collector = Optional.empty();
    } catch (ValidationException e) {
      try {
        LiftedType liftedType = LiftedType.lift(basicInfo.originalReturnType(), tool());
        mapperType = new MapperClassAnalyzer(basicInfo, liftedType.liftedType(), mapperClass).checkReturnType();
        extractExpr = liftedType.extractExpr();
        constructorParamType = basicInfo.returnType();
        collector = Optional.empty();
      } catch (ValidationException e1) {
        Optional<TypeMirror> wrappedType = tool().unwrap(List.class, basicInfo.originalReturnType());
        if (!wrappedType.isPresent()) {
          throw e1;
        }
        mapperType = new MapperClassAnalyzer(basicInfo, wrappedType.get(), mapperClass).checkReturnType();
        extractExpr = p -> CodeBlock.of("$N", p);
        constructorParamType = basicInfo.returnType();
        collector = Optional.of(new DefaultCollector(wrappedType.get()));
      }
    }
    return Coercion.getCoercion(new ExplicitMapperCoercion(mapperType),
        basicInfo, collector, mapperType, extractExpr, constructorParamType);
  }

  private Coercion handleRepeatableAutoMapper() {
    AbstractCollector collectorInfo = collectorInfo();
    CoercionFactory coercion = findCoercion(collectorInfo.inputType())
        .orElseThrow(() -> basicInfo.asValidationException("Unknown parameter type. Define a custom mapper."));
    MapperType mapperType = MapperType.create(collectorInfo.inputType(), coercion.createMapper(collectorInfo.inputType()), true);
    Function<ParameterSpec, CodeBlock> extractExpr = p -> CodeBlock.of("$N", p);
    TypeMirror constructorParamType = basicInfo.originalReturnType();
    return Coercion.getCoercion(coercion, basicInfo, Optional.of(collectorInfo), mapperType, extractExpr, constructorParamType);
  }

  private Coercion handleRepeatableExplicitMapper(TypeElement mapperClass) {
    AbstractCollector collectorInfo = collectorInfo();
    ReferenceMapperType mapperType = new MapperClassValidator(basicInfo, collectorInfo.inputType(), mapperClass).checkReturnType();
    Function<ParameterSpec, CodeBlock> extractExpr = p -> CodeBlock.of("$N", p);
    TypeMirror constructorParamType = basicInfo.originalReturnType();
    return Coercion.getCoercion(new ExplicitMapperCoercion(mapperType),
        basicInfo, Optional.of(collectorInfo), mapperType, extractExpr, constructorParamType);
  }

  private Optional<CoercionFactory> findCoercion(TypeMirror innerType) {
    CoercionFactory standardCoercion = StandardCoercions.get(tool(), tool().box(innerType));
    if (standardCoercion != null) {
      return Optional.of(standardCoercion);
    }
    boolean isEnum = isEnumType(innerType);
    if (!isEnum) {
      return Optional.empty();
    }
    return Optional.of(EnumCoercion.create());
  }

  private boolean isEnumType(TypeMirror mirror) {
    List<? extends TypeMirror> supertypes = tool().getDirectSupertypes(mirror);
    if (supertypes.isEmpty()) {
      // not an enum
      return false;
    }
    TypeMirror superclass = supertypes.get(0);
    if (!tool().isSameErasure(superclass, tool().asType(Enum.class))) {
      // not an enum
      return false;
    }
    if (tool().isPrivateType(mirror)) {
      throw basicInfo.asValidationException("The enum may not be private.");
    }
    return true;
  }

  private AbstractCollector collectorInfo() {
    if (basicInfo.collectorClass().isPresent()) {
      return new CollectorClassValidator(basicInfo, basicInfo.collectorClass().get()).getCollectorInfo();
    }
    if (!tool().isSameErasure(basicInfo.returnType(), List.class)) {
      throw basicInfo.asValidationException("Either define a custom collector, or return List.");
    }
    List<? extends TypeMirror> typeParameters = tool().typeargs(basicInfo.returnType());
    if (typeParameters.isEmpty()) {
      throw basicInfo.asValidationException("Add a type parameter.");
    }
    return new DefaultCollector(typeParameters.get(0));
  }

  private TypeTool tool() {
    return basicInfo.tool();
  }
}
