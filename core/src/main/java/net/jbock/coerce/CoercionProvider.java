package net.jbock.coerce;

import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import net.jbock.coerce.hint.HintProvider;
import net.jbock.coerce.mappers.CoercionFactory;
import net.jbock.coerce.mappers.EnumCoercion;
import net.jbock.coerce.mappers.MapperCoercion;
import net.jbock.coerce.mappers.StandardCoercions;
import net.jbock.compiler.TypeTool;
import net.jbock.compiler.ValidationException;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import java.util.List;
import java.util.Optional;

import static net.jbock.coerce.OptionalInfo.findOptionalInfo;
import static net.jbock.compiler.Util.snakeToCamel;

public class CoercionProvider {

  private static CoercionProvider instance;

  public static CoercionProvider getInstance() {
    if (instance == null) {
      instance = new CoercionProvider();
    }
    return instance;
  }

  public Coercion findCoercion(
      ExecutableElement sourceMethod,
      String paramName,
      TypeElement mapperClass,
      TypeElement collectorClass,
      boolean repeatable,
      boolean optional) {
    sanityChecks(sourceMethod, collectorClass, repeatable, optional);
    TypeMirror returnType = sourceMethod.getReturnType();
    BasicInfo basicInfo = BasicInfo.create(repeatable, optional, returnType, paramName, sourceMethod, TypeTool.get());
    if (basicInfo.repeatable) {
      try {
        return handleRepeatable(basicInfo, paramName, mapperClass, collectorClass);
      } catch (UnknownTypeException e) {
        Optional<String> hint = HintProvider.instance().findHint(basicInfo);
        throw hint.map(m -> e.asValidationException(sourceMethod, m))
            .orElseGet(() -> e.asValidationException(sourceMethod));
      }
    }
    Optional<TypeMirror> optionalInfo = findOptionalInfo(basicInfo);
    try {
      return handleNonRepeatable(optionalInfo, basicInfo, paramName, mapperClass);
    } catch (UnknownTypeException e) {
      Optional<String> hint = HintProvider.instance().findHint(optionalInfo, basicInfo);
      throw hint.map(m -> e.asValidationException(sourceMethod, m))
          .orElseGet(() -> e.asValidationException(sourceMethod));
    }
  }

  private void sanityChecks(ExecutableElement sourceMethod, TypeElement collectorClass, boolean repeatable, boolean optional) {
    if (repeatable && optional) {
      throw ValidationException.create(sourceMethod,
          "The parameter can be repeatable or optional, but not both.");
    }
    if (collectorClass != null && !repeatable) {
      throw ValidationException.create(sourceMethod,
          "The parameter must be declared repeatable in order to have a collector.");
    }
  }

  private Coercion handleNonRepeatable(
      Optional<TypeMirror> optionalInfo,
      BasicInfo basicInfo,
      String paramName,
      TypeElement mapperClass) throws UnknownTypeException {
    boolean auto = mapperClass == null;
    if (auto) {
      return handleSingleAuto(optionalInfo, basicInfo);
    } else {
      return handleSingle(optionalInfo, paramName, mapperClass, basicInfo);
    }
  }

  private Coercion handleRepeatable(
      BasicInfo basicInfo,
      String paramName,
      TypeElement mapperClass,
      TypeElement collectorClass) throws UnknownTypeException {
    boolean auto = mapperClass == null;
    if (auto) {
      return handleRepeatableAuto(collectorClass, basicInfo);
    } else {
      return handleRepeatableNonAuto(paramName, mapperClass, collectorClass, basicInfo);
    }
  }

  // no mapper, not repeatable
  private Coercion handleSingleAuto(
      Optional<TypeMirror> optionalInfo,
      BasicInfo basicInfo) throws UnknownTypeException {
    CoercionFactory factory = findCoercion(basicInfo, optionalInfo.orElse(basicInfo.returnType()));
    return factory.getCoercion(basicInfo, optionalInfo, Optional.empty());
  }

  // simple mapper
  private Coercion handleSingle(
      Optional<TypeMirror> optionalInfo,
      String paramName,
      TypeElement mapperClass,
      BasicInfo basicInfo) {
    TypeMirror returnType = basicInfo.returnType();
    ParameterSpec mapperParam = ParameterSpec.builder(TypeName.get(mapperClass.asType()), snakeToCamel(paramName) + "Mapper").build();
    TypeMirror mapperReturnType = optionalInfo.orElse(returnType);
    MapperClassValidator.checkReturnType(mapperClass, mapperReturnType, basicInfo);
    return MapperCoercion.create(mapperReturnType, optionalInfo, Optional.empty(), mapperParam, mapperClass.asType(), basicInfo);
  }

  // repeatable with mapper
  private Coercion handleRepeatableNonAuto(
      String paramName,
      TypeElement mapperClass,
      TypeElement collectorClass,
      BasicInfo basicInfo) {
    CollectorInfo collectorInfo = collectorInfo(basicInfo, collectorClass);
    MapperClassValidator.checkReturnType(mapperClass, collectorInfo.inputType, basicInfo);
    ParameterSpec mapperParam = ParameterSpec.builder(TypeName.get(mapperClass.asType()), snakeToCamel(paramName) + "Mapper").build();
    return MapperCoercion.create(collectorInfo.inputType, Optional.empty(), collectorInfo.collectorType(), mapperParam, mapperClass.asType(), basicInfo);
  }

  // repeatable without mapper
  private Coercion handleRepeatableAuto(
      TypeElement collectorClass,
      BasicInfo basicInfo) throws UnknownTypeException {
    CollectorInfo collectorInfo = collectorInfo(basicInfo, collectorClass);
    CoercionFactory coercion = findCoercion(basicInfo, collectorInfo.inputType);
    return coercion.getCoercion(basicInfo, Optional.empty(), collectorInfo.collectorType());
  }

  private CoercionFactory findCoercion(BasicInfo basicInfo, TypeMirror mirror) throws UnknownTypeException {
    CoercionFactory standardCoercion = StandardCoercions.get(mirror);
    if (standardCoercion != null) {
      return standardCoercion;
    }
    boolean isEnum = isEnumType(basicInfo, mirror);
    if (!isEnum) {
      throw UnknownTypeException.create();
    }
    return EnumCoercion.create(mirror);
  }

  private boolean isEnumType(BasicInfo basicInfo, TypeMirror mirror) {
    TypeTool tool = TypeTool.get();
    List<? extends TypeMirror> supertypes = tool.getDirectSupertypes(mirror);
    if (supertypes.isEmpty()) {
      // not an enum
      return false;
    }
    TypeMirror superclass = supertypes.get(0);
    if (!tool.isSameErasure(superclass, tool.asType(Enum.class))) {
      // not an enum
      return false;
    }
    if (tool.isPrivateType(mirror)) {
      throw basicInfo.asValidationException("The enum may not be private.");
    }
    return true;
  }

  private CollectorInfo collectorInfo(
      BasicInfo basicInfo,
      TypeElement collectorClass) {
    if (collectorClass != null) {
      return CollectorClassValidator.getCollectorInfo(collectorClass, basicInfo);
    }
    TypeTool tool = TypeTool.get();
    if (!tool.isSameErasure(basicInfo.returnType(), List.class)) {
      throw basicInfo.asValidationException("Either define a custom collector, or return List.");
    }
    List<? extends TypeMirror> typeParameters = tool.typeargs(basicInfo.returnType());
    if (typeParameters.isEmpty()) {
      throw basicInfo.asValidationException("Add a type parameter.");
    }
    return CollectorInfo.listCollector(typeParameters.get(0));
  }
}
