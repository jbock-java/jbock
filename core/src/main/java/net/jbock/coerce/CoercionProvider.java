package net.jbock.coerce;

import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import net.jbock.coerce.mappers.CoercionFactory;
import net.jbock.coerce.mappers.EnumCoercion;
import net.jbock.coerce.mappers.MapperCoercion;
import net.jbock.coerce.mappers.StandardCoercions;
import net.jbock.coerce.hint.HintProvider;
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
    try {
      return handle(sourceMethod, paramName, mapperClass, collectorClass, repeatable, optional);
    } catch (TmpException e) {
      throw e.asValidationException(sourceMethod);
    } catch (SearchHintException e) {
      Optional<String> hint = HintProvider.instance().findHint(returnType, repeatable, optional);
      throw hint.map(m -> e.asValidationException(sourceMethod, m))
          .orElseGet(() -> e.asValidationException(sourceMethod));
    }
  }

  private Coercion handle(
      ExecutableElement sourceMethod,
      String paramName,
      TypeElement mapperClass,
      TypeElement collectorClass,
      boolean repeatable,
      boolean optional) throws TmpException, SearchHintException {
    BasicInfo basicInfo = new BasicInfo(sourceMethod.getReturnType(), paramName);
    boolean auto = mapperClass == null;
    if (repeatable) {
      if (auto) {
        return handleRepeatableAuto(sourceMethod, collectorClass, basicInfo);
      } else {
        return handleRepeatable(sourceMethod, paramName, mapperClass, collectorClass, basicInfo);
      }
    } else {
      if (auto) {
        return handleSingleAuto(sourceMethod, optional, basicInfo);
      } else {
        return handleSingle(sourceMethod, paramName, mapperClass, basicInfo, optional);
      }
    }
  }

  // no mapper or collector
  private Coercion handleSingleAuto(
      ExecutableElement sourceMethod,
      boolean optional,
      BasicInfo basicInfo) throws TmpException, SearchHintException {
    TypeMirror returnType = sourceMethod.getReturnType();
    OptionalInfo optionalInfo = findOptionalInfo(returnType, optional);
    CoercionFactory enumCoercion = checkEnum(optionalInfo.baseType);
    if (enumCoercion != null) {
      return enumCoercion.getCoercion(basicInfo, optionalInfo, Optional.empty());
    }
    CoercionFactory factory = StandardCoercions.get(optionalInfo.baseType);
    if (factory == null) {
      throw SearchHintException.create("Unknown parameter type. Define a custom mapper.");
    }
    if (factory.handlesOptionalPrimitive() && !optional) {
      throw TmpException.create("Declare this parameter optional.");
    }
    if (optional && !factory.handlesOptionalPrimitive() && !optionalInfo.optional) {
      throw TmpException.create("Wrap the parameter type in Optional");
    }
    return factory.getCoercion(basicInfo, optionalInfo, Optional.empty());
  }

  // mapper but no collector
  private Coercion handleSingle(
      ExecutableElement sourceMethod,
      String paramName,
      TypeElement mapperClass,
      BasicInfo basicInfo,
      boolean optional) throws TmpException {
    ParameterSpec mapperParam = ParameterSpec.builder(TypeName.get(mapperClass.asType()), snakeToCamel(paramName) + "Mapper").build();
    OptionalInfo optionalInfo = findOptionalInfo(sourceMethod.getReturnType(), optional);
    if (optional && !optionalInfo.optional) {
      throw TmpException.create("Wrap the parameter type in Optional");
    }
    TypeMirror mapperType = MapperClassValidator.checkReturnType(mapperClass, optionalInfo.baseType);
    return MapperCoercion.create(optionalInfo, mapperParam, mapperType, basicInfo);
  }

  // mapper and collector
  private Coercion handleRepeatable(
      ExecutableElement sourceMethod,
      String paramName,
      TypeElement mapperClass,
      TypeElement collectorClass,
      BasicInfo basicInfo) throws TmpException {
    CollectorInfo collectorInfo = collectorInfo(sourceMethod, collectorClass);
    MapperClassValidator.checkReturnType(mapperClass, collectorInfo.inputType);
    ParameterSpec mapperParam = ParameterSpec.builder(TypeName.get(mapperClass.asType()), snakeToCamel(paramName) + "Mapper").build();
    TypeMirror mapperType = MapperClassValidator.checkReturnType(mapperClass, collectorInfo.inputType);
    return MapperCoercion.create(OptionalInfo.simple(collectorInfo.inputType), collectorInfo, mapperParam, mapperType, basicInfo);
  }

  // collector but no mapper
  private Coercion handleRepeatableAuto(
      ExecutableElement sourceMethod,
      TypeElement collectorClass,
      BasicInfo basicInfo) throws TmpException {
    CollectorInfo collectorInfo = collectorInfo(sourceMethod, collectorClass);
    CoercionFactory coercion = StandardCoercions.get(collectorInfo.inputType);
    if (coercion == null) {
      coercion = checkEnum(collectorInfo.inputType);
    }
    if (coercion == null || coercion.handlesOptionalPrimitive()) {
      throw TmpException.create(String.format("Define a mapper for %s", collectorInfo.inputType));
    }
    return coercion.getCoercion(basicInfo, OptionalInfo.simple(collectorInfo.inputType), Optional.of(collectorInfo));
  }

  private CoercionFactory checkEnum(TypeMirror mirror) throws TmpException {
    TypeTool tool = TypeTool.get();
    List<? extends TypeMirror> supertypes = tool.getDirectSupertypes(mirror);
    if (supertypes.isEmpty()) {
      // not an enum
      return null;
    }
    TypeMirror superclass = supertypes.get(0);
    if (!tool.eql(tool.erasure(superclass), tool.declared(Enum.class))) {
      // not an enum
      return null;
    }
    if (tool.isPrivateType(mirror)) {
      throw TmpException.create("The enum may not be private.");
    }
    return EnumCoercion.create(mirror);
  }

  private CollectorInfo collectorInfo(
      ExecutableElement sourceMethod,
      TypeElement collectorClass) throws TmpException {
    if (collectorClass == null) {
      TypeTool tool = TypeTool.get();
      if (!tool.eql(tool.erasure(sourceMethod.getReturnType()), tool.declared(List.class))) {
        throw TmpException.create("Either define a custom collector, or return List");
      }
      List<? extends TypeMirror> typeParameters = tool.typeargs(sourceMethod.getReturnType());
      if (typeParameters.isEmpty()) {
        throw TmpException.create("Either define a custom collector, or return List");
      }
      return CollectorInfo.listCollector(typeParameters.get(0));
    }
    return CollectorClassValidator.getCollectorInfo(sourceMethod.getReturnType(), collectorClass);
  }
}
