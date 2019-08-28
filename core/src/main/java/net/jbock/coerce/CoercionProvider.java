package net.jbock.coerce;

import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import net.jbock.coerce.hint.HintProvider;
import net.jbock.coerce.mappers.CoercionFactory;
import net.jbock.coerce.mappers.EnumCoercion;
import net.jbock.coerce.mappers.MapperCoercion;
import net.jbock.coerce.mappers.StandardCoercions;
import net.jbock.compiler.TypeTool;

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
      InferredAttributes attributes,
      TypeTool tool) {
    BasicInfo basicInfo = BasicInfo.create(
        mapperClass, collectorClass,
        attributes, paramName, sourceMethod, tool);
    CoercionProvider coercionProvider = new CoercionProvider(basicInfo);
    return coercionProvider.run();
  }

  private Coercion run() {
    try {
      if (basicInfo.isRepeatable()) {
        return handleRepeatable();
      } else {
        return handle();
      }
    } catch (UnknownTypeException e) {
      Optional<String> hint = HintProvider.instance().findHint(basicInfo);
      throw hint.map(m -> e.asValidationException(basicInfo.sourceMethod(), m))
          .orElseGet(() -> e.asValidationException(basicInfo.sourceMethod()));
    }
  }

  private Coercion handle() throws UnknownTypeException {
    if (basicInfo.mapperClass().isPresent()) {
      return handleExplicitMapper(basicInfo.mapperClass().get());
    } else {
      return handleAutoMapper();
    }
  }

  private Coercion handleRepeatable() throws UnknownTypeException {
    if (basicInfo.mapperClass().isPresent()) {
      return handleRepeatableExplicitMapper(basicInfo.mapperClass().get());
    } else {
      return handleRepeatableAutoMapper();
    }
  }

  private Coercion handleAutoMapper() throws UnknownTypeException {
    CoercionFactory factory = findCoercion(basicInfo.optionalInfo().orElse(basicInfo.returnType()));
    return factory.getCoercion(basicInfo, Optional.empty());
  }

  private Coercion handleExplicitMapper(TypeElement mapperClass) {
    TypeMirror returnType = basicInfo.returnType();
    ParameterSpec mapperParam = ParameterSpec.builder(TypeName.get(mapperClass.asType()), snakeToCamel(basicInfo.paramName()) + "Mapper").build();
    TypeMirror mapperReturnType = basicInfo.optionalInfo().orElse(returnType);
    MapperType mapperType = new MapperClassValidator(basicInfo).checkReturnType(mapperClass, mapperReturnType);
    return MapperCoercion.create(mapperReturnType, Optional.empty(), mapperParam, mapperType, basicInfo);
  }

  private Coercion handleRepeatableAutoMapper() throws UnknownTypeException {
    CollectorInfo collectorInfo = collectorInfo();
    CoercionFactory coercion = findCoercion(collectorInfo.inputType);
    return coercion.getCoercion(basicInfo, collectorInfo.collectorType());
  }

  private Coercion handleRepeatableExplicitMapper(
      TypeElement mapperClass) {
    CollectorInfo collectorInfo = collectorInfo();
    MapperType mapperType = new MapperClassValidator(basicInfo).checkReturnType(mapperClass, collectorInfo.inputType);
    ParameterSpec mapperParam = ParameterSpec.builder(TypeName.get(mapperClass.asType()), snakeToCamel(basicInfo.paramName()) + "Mapper").build();
    return MapperCoercion.create(collectorInfo.inputType, collectorInfo.collectorType(), mapperParam, mapperType, basicInfo);
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

  private CollectorInfo collectorInfo() {
    if (basicInfo.collectorClass().isPresent()) {
      return new CollectorClassValidator(basicInfo).getCollectorInfo(basicInfo.collectorClass().get());
    }
    if (!tool().isSameErasure(basicInfo.returnType(), List.class)) {
      throw basicInfo.asValidationException("Either define a custom collector, or return List.");
    }
    List<? extends TypeMirror> typeParameters = tool().typeargs(basicInfo.returnType());
    if (typeParameters.isEmpty()) {
      throw basicInfo.asValidationException("Add a type parameter.");
    }
    return CollectorInfo.listCollector(typeParameters.get(0));
  }

  private TypeTool tool() {
    return basicInfo.tool();
  }
}
