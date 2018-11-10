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
    if (repeatable && optional) {
      throw ValidationException.create(sourceMethod,
          "The parameter can be repeatable or optional, but not both.");
    }
    if (collectorClass != null && !repeatable) {
      throw ValidationException.create(sourceMethod,
          "The parameter must be declared repeatable in order to have a collector.");
    }
    TypeMirror returnType = sourceMethod.getReturnType();
    BasicInfo basicInfo = BasicInfo.create(repeatable, optional, returnType, paramName);
    Optional<TypeMirror> optionalInfo;
    try {
      optionalInfo = repeatable ? Optional.empty() : findOptionalInfo(basicInfo);
    } catch (TmpException e) {
      throw e.asValidationException(sourceMethod);
    }
    try {
      return handle(optionalInfo, basicInfo, paramName, mapperClass, collectorClass);
    } catch (TmpException e) {
      throw e.asValidationException(sourceMethod);
    } catch (UnknownTypeException e) {
      Optional<String> hint = HintProvider.instance().findHint(optionalInfo, basicInfo);
      throw hint.map(m -> e.asValidationException(sourceMethod, m))
          .orElseGet(() -> e.asValidationException(sourceMethod));
    }
  }

  private Coercion handle(
      Optional<TypeMirror> optionalInfo,
      BasicInfo basicInfo,
      String paramName,
      TypeElement mapperClass,
      TypeElement collectorClass) throws TmpException, UnknownTypeException {
    boolean auto = mapperClass == null;
    if (basicInfo.repeatable) {
      if (auto) {
        return handleRepeatableAuto(collectorClass, basicInfo);
      } else {
        return handleRepeatable(paramName, mapperClass, collectorClass, basicInfo);
      }
    } else {
      if (auto) {
        return handleSingleAuto(optionalInfo, basicInfo);
      } else {
        return handleSingle(optionalInfo, paramName, mapperClass, basicInfo);
      }
    }
  }

  // no mapper, not repeatable
  private Coercion handleSingleAuto(
      Optional<TypeMirror> optionalInfo,
      BasicInfo basicInfo) throws TmpException, UnknownTypeException {
    CoercionFactory factory = findCoercion(optionalInfo.orElse(basicInfo.returnType()));
    return factory.getCoercion(basicInfo, optionalInfo, Optional.empty());
  }

  // simple mapper
  private Coercion handleSingle(
      Optional<TypeMirror> optionalInfo,
      String paramName,
      TypeElement mapperClass,
      BasicInfo basicInfo) throws TmpException {
    TypeMirror returnType = basicInfo.returnType();
    ParameterSpec mapperParam = ParameterSpec.builder(TypeName.get(mapperClass.asType()), snakeToCamel(paramName) + "Mapper").build();
    TypeMirror mapperReturnType = optionalInfo.orElse(returnType);
    MapperClassValidator.checkReturnType(mapperClass, mapperReturnType);
    return MapperCoercion.create(mapperReturnType, optionalInfo, Optional.empty(), mapperParam, mapperClass.asType(), basicInfo);
  }

  // repeatable with mapper
  private Coercion handleRepeatable(
      String paramName,
      TypeElement mapperClass,
      TypeElement collectorClass,
      BasicInfo basicInfo) throws TmpException {
    CollectorInfo collectorInfo = collectorInfo(basicInfo.returnType(), collectorClass);
    MapperClassValidator.checkReturnType(mapperClass, collectorInfo.inputType);
    ParameterSpec mapperParam = ParameterSpec.builder(TypeName.get(mapperClass.asType()), snakeToCamel(paramName) + "Mapper").build();
    return MapperCoercion.create(collectorInfo.inputType, Optional.empty(), collectorInfo.collectorType(), mapperParam, mapperClass.asType(), basicInfo);
  }

  // repeatable without mapper
  private Coercion handleRepeatableAuto(
      TypeElement collectorClass,
      BasicInfo basicInfo) throws TmpException, UnknownTypeException {
    CollectorInfo collectorInfo = collectorInfo(basicInfo.returnType(), collectorClass);
    CoercionFactory coercion = findCoercion(collectorInfo.inputType);
    return coercion.getCoercion(basicInfo, Optional.empty(), collectorInfo.collectorType());
  }

  private CoercionFactory findCoercion(TypeMirror mirror) throws TmpException, UnknownTypeException {
    CoercionFactory standardCoercion = StandardCoercions.get(mirror);
    if (standardCoercion != null) {
      return standardCoercion;
    }
    boolean isEnum = isEnumType(mirror);
    if (!isEnum) {
      throw UnknownTypeException.create();
    }
    return EnumCoercion.create(mirror);
  }

  private boolean isEnumType(TypeMirror mirror) throws TmpException {
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
      throw TmpException.create("The enum may not be private.");
    }
    return true;
  }

  private CollectorInfo collectorInfo(
      TypeMirror returnType,
      TypeElement collectorClass) throws TmpException {
    if (collectorClass != null) {
      return CollectorClassValidator.getCollectorInfo(returnType, collectorClass);
    }
    TypeTool tool = TypeTool.get();
    if (!tool.isSameErasure(returnType, List.class)) {
      throw TmpException.create("Either define a custom collector, or return List.");
    }
    List<? extends TypeMirror> typeParameters = tool.typeargs(returnType);
    if (typeParameters.isEmpty()) {
      throw TmpException.create("Add a type parameter.");
    }
    return CollectorInfo.listCollector(typeParameters.get(0));
  }
}
