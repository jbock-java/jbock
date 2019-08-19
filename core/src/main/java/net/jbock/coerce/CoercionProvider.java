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

import static net.jbock.compiler.Util.snakeToCamel;

public class CoercionProvider {

  private final BasicInfo basicInfo;

  private CoercionProvider(BasicInfo basicInfo) {
    this.basicInfo = basicInfo;
  }

  public static Coercion findCoercion(
      ExecutableElement sourceMethod,
      String paramName,
      TypeElement mapperClass,
      TypeElement collectorClass,
      boolean repeatable,
      boolean optional) {
    sanityChecks(sourceMethod, collectorClass, repeatable, optional);
    TypeMirror returnType = sourceMethod.getReturnType();
    BasicInfo basicInfo = BasicInfo.create(repeatable, optional, returnType, paramName, sourceMethod, TypeTool.get());
    CoercionProvider coercionProvider = new CoercionProvider(basicInfo);
    return coercionProvider.run(paramName, mapperClass, collectorClass);
  }

  private Coercion run(
      String paramName,
      TypeElement mapperClass,
      TypeElement collectorClass) {
    if (basicInfo.repeatable) {
      try {
        return handleRepeatable(paramName, mapperClass, collectorClass);
      } catch (UnknownTypeException e) {
        Optional<String> hint = HintProvider.instance().findHint(basicInfo);
        throw hint.map(m -> e.asValidationException(basicInfo.sourceMethod(), m))
            .orElseGet(() -> e.asValidationException(basicInfo.sourceMethod()));
      }
    }
    try {
      return handleNonRepeatable(paramName, mapperClass);
    } catch (UnknownTypeException e) {
      Optional<String> hint = HintProvider.instance().findHint(basicInfo);
      throw hint.map(m -> e.asValidationException(basicInfo.sourceMethod(), m))
          .orElseGet(() -> e.asValidationException(basicInfo.sourceMethod()));
    }
  }

  private static void sanityChecks(ExecutableElement sourceMethod, TypeElement collectorClass, boolean repeatable, boolean optional) {
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
      String paramName,
      TypeElement mapperClass) throws UnknownTypeException {
    boolean auto = mapperClass == null;
    if (auto) {
      return handleSingleAuto();
    } else {
      return handleSingle(paramName, mapperClass);
    }
  }

  private Coercion handleRepeatable(
      String paramName,
      TypeElement mapperClass,
      TypeElement collectorClass) throws UnknownTypeException {
    boolean auto = mapperClass == null;
    if (auto) {
      return handleRepeatableAuto(collectorClass);
    } else {
      return handleRepeatableNonAuto(paramName, mapperClass, collectorClass);
    }
  }

  // no mapper, not repeatable
  private Coercion handleSingleAuto() throws UnknownTypeException {
    CoercionFactory factory = findCoercion(basicInfo.optionalInfo().orElse(basicInfo.returnType()));
    return factory.getCoercion(basicInfo, Optional.empty());
  }

  // simple mapper
  private Coercion handleSingle(
      String paramName,
      TypeElement mapperClass) {
    TypeMirror returnType = basicInfo.returnType();
    ParameterSpec mapperParam = ParameterSpec.builder(TypeName.get(mapperClass.asType()), snakeToCamel(paramName) + "Mapper").build();
    TypeMirror mapperReturnType = basicInfo.optionalInfo().orElse(returnType);
    MapperClassValidator.checkReturnType(mapperClass, mapperReturnType, basicInfo);
    return MapperCoercion.create(mapperReturnType, Optional.empty(), mapperParam, mapperClass.asType(), basicInfo);
  }

  // repeatable with mapper
  private Coercion handleRepeatableNonAuto(
      String paramName,
      TypeElement mapperClass,
      TypeElement collectorClass) {
    CollectorInfo collectorInfo = collectorInfo(collectorClass);
    MapperClassValidator.checkReturnType(mapperClass, collectorInfo.inputType, basicInfo);
    ParameterSpec mapperParam = ParameterSpec.builder(TypeName.get(mapperClass.asType()), snakeToCamel(paramName) + "Mapper").build();
    return MapperCoercion.create(collectorInfo.inputType, collectorInfo.collectorType(), mapperParam, mapperClass.asType(), basicInfo);
  }

  // repeatable without mapper
  private Coercion handleRepeatableAuto(
      TypeElement collectorClass) throws UnknownTypeException {
    CollectorInfo collectorInfo = collectorInfo(collectorClass);
    CoercionFactory coercion = findCoercion(collectorInfo.inputType);
    return coercion.getCoercion(basicInfo, collectorInfo.collectorType());
  }

  private CoercionFactory findCoercion(TypeMirror mirror) throws UnknownTypeException {
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

  private boolean isEnumType(TypeMirror mirror) {
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

  private CollectorInfo collectorInfo(TypeElement collectorClass) {
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
