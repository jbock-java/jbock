package net.jbock.coerce;

import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import net.jbock.coerce.mappers.CoercionFactory;
import net.jbock.coerce.mappers.EnumCoercion;
import net.jbock.coerce.mappers.MapperCoercion;
import net.jbock.coerce.mappers.StandardCoercions;
import net.jbock.coerce.warn.WarningProvider;
import net.jbock.compiler.TypeTool;
import net.jbock.compiler.ValidationException;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import java.util.List;

import static javax.lang.model.element.Modifier.FINAL;
import static net.jbock.coerce.CoercionKind.findKind;
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
      if (!e.findWarning()) {
        throw e.asValidationException(sourceMethod);
      }
      String warning = WarningProvider.instance().findWarning(returnType, repeatable, optional);
      if (warning != null) {
        throw e.asValidationException(sourceMethod, warning);
      }
      throw e.asValidationException(sourceMethod);
    }
  }

  private Coercion handle(
      ExecutableElement sourceMethod,
      String paramName,
      TypeElement mapperClass,
      TypeElement collectorClass,
      boolean repeatable,
      boolean optional) throws TmpException {
    FieldSpec field = FieldSpec.builder(TypeName.get(sourceMethod.getReturnType()),
        snakeToCamel(paramName))
        .addModifiers(FINAL)
        .build();
    if (repeatable) {
      return handleRepeatable(sourceMethod, paramName, mapperClass, collectorClass, field);
    }
    if (mapperClass != null) {
      return handleMapper(sourceMethod, paramName, mapperClass, field, optional);
    }
    return handleSimple(sourceMethod, optional, field);
  }

  // no mapper or collector
  private Coercion handleSimple(
      ExecutableElement sourceMethod,
      boolean optional,
      FieldSpec field) throws TmpException {
    TypeMirror returnType = sourceMethod.getReturnType();
    TriggerKind tk = trigger(returnType, optional);
    return handleDefault(tk, field, optional);
  }

  private Coercion handleRepeatable(
      ExecutableElement sourceMethod,
      String paramName,
      TypeElement mapperClass,
      TypeElement collectorClass,
      FieldSpec field) throws TmpException {
    if (mapperClass == null) {
      return handleRepeatableNoMapper(sourceMethod, collectorClass, field);
    }
    CollectorInfo collectorInput = collectorInput(sourceMethod, collectorClass);
    MapperClassValidator.checkReturnType(mapperClass, collectorInput.collectorInput);
    collectorInput = collectorInput.withInput(collectorInput.collectorInput);
    TriggerKind tk = CoercionKind.SIMPLE.of(collectorInput.collectorInput, collectorInput);
    ParameterSpec mapperParam = ParameterSpec.builder(TypeName.get(mapperClass.asType()), snakeToCamel(paramName) + "Mapper").build();
    TypeMirror mapperType = MapperClassValidator.checkReturnType(mapperClass, tk.trigger);
    return MapperCoercion.create(tk, mapperParam, mapperType, field);
  }

  private Coercion handleRepeatableNoMapper(
      ExecutableElement sourceMethod,
      TypeElement collectorClass,
      FieldSpec field) throws TmpException {
    CollectorInfo collectorInput = collectorInput(sourceMethod, collectorClass);
    CoercionFactory coercion = StandardCoercions.get(collectorInput.collectorInput);
    if (coercion == null) {
      throw TmpException.findWarning(String.format("Unknown collector input %s, please define a custom mapper.", collectorInput.collectorInput));
    }
    TriggerKind tk = CoercionKind.SIMPLE.of(collectorInput.collectorInput, collectorInput);
    return coercion.getCoercion(field, tk);
  }

  // mapper but no collector
  private Coercion handleMapper(
      ExecutableElement sourceMethod,
      String paramName,
      TypeElement mapperClass,
      FieldSpec field,
      boolean optional) throws TmpException {
    ParameterSpec mapperParam = ParameterSpec.builder(TypeName.get(mapperClass.asType()), snakeToCamel(paramName) + "Mapper").build();
    TriggerKind tk = trigger(sourceMethod.getReturnType(), optional);
    if (optional && !tk.kind.isWrappedInOptional()) {
      throw TmpException.create("Wrap the parameter type in Optional");
    }
    TypeMirror mapperType = MapperClassValidator.checkReturnType(mapperClass, tk.trigger);
    return MapperCoercion.create(tk, mapperParam, mapperType, field);
  }

  private Coercion handleDefault(
      TriggerKind tk,
      FieldSpec field,
      boolean optional) throws TmpException {
    CoercionFactory enumCoercion = checkEnum(tk.trigger);
    if (enumCoercion != null) {
      return enumCoercion.getCoercion(field, tk);
    }
    CoercionFactory factory = StandardCoercions.get(tk.trigger);
    if (factory == null) {
      throw TmpException.findWarning("Bad return type");
    }
    if (factory.handlesOptionalPrimitive() && !optional) {
      throw TmpException.create("Declare this parameter optional.");
    }
    if (optional && !factory.handlesOptionalPrimitive() && !tk.kind.isWrappedInOptional()) {
      throw TmpException.create("Wrap the parameter type in Optional");
    }
    return factory.getCoercion(field, tk);
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
      throw TmpException.findWarning("The enum may not be private.");
    }
    return EnumCoercion.create(mirror);
  }

  private TriggerKind trigger(
      TypeMirror returnType,
      boolean optional) {
    if (optional) {
      return findKind(returnType);
    }
    return CoercionKind.SIMPLE.of(returnType, CollectorInfo.empty());
  }

  private CollectorInfo collectorInput(
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
    CollectorClassValidator.CollectorResult collectorResult = CollectorClassValidator.findInputType(sourceMethod.getReturnType(), collectorClass);
    return CollectorInfo.create(collectorResult.inputType, collectorResult.collectorType);
  }
}
