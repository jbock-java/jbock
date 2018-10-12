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
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.util.List;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;

import static javax.lang.model.element.Modifier.FINAL;
import static net.jbock.coerce.CoercionKind.findKind;
import static net.jbock.coerce.mappers.MapperCoercion.mapperInit;
import static net.jbock.coerce.mappers.MapperCoercion.mapperMap;

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
    if (returnType.getKind() == TypeKind.ARRAY) {
      // there's no default mapper for array
      throw TmpException.create("Either switch to List and declare this parameter repeatable, or use a custom mapper.");
    }
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
    TypeName mapperType = TypeName.get(mapperClass.asType());
    ParameterSpec mapperParam = ParameterSpec.builder(mapperType, snakeToCamel(paramName) + "Mapper").build();
    return MapperCoercion.create(tk, mapperParam, mapperClass.asType(), field);
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

  private Coercion handleMapper(
      ExecutableElement sourceMethod,
      String paramName,
      TypeElement mapperClass,
      FieldSpec field,
      boolean optional) throws TmpException {
    TypeName mapperType = TypeName.get(mapperClass.asType());
    ParameterSpec mapperParam = ParameterSpec.builder(mapperType, snakeToCamel(paramName) + "Mapper").build();
    TriggerKind tk = trigger(sourceMethod.getReturnType(), optional);
    Coercion skewedCoercion = skewedCoercion(tk, field, mapperParam, mapperClass);
    if (skewedCoercion != null) {
      return skewedCoercion;
    }
    MapperClassValidator.checkReturnType(mapperClass, tk.trigger);
    return MapperCoercion.create(tk, mapperParam, mapperClass.asType(), field);
  }

  private Coercion skewedCoercion(
      TriggerKind tk,
      FieldSpec field,
      ParameterSpec mapperParam,
      TypeElement mapperClass) throws TmpException {
    if (tk.trigger.getKind().isPrimitive()) {
      return skewedCoercion(tk, field, mapperParam, mapperClass, TypeTool.get().box(tk.trigger));
    }
    if (TypeTool.get().eql(tk.trigger, OptionalInt.class)) {
      return skewedCoercion(tk, field, mapperParam, mapperClass, TypeTool.get().declared(Integer.class), tk.trigger);
    }
    if (TypeTool.get().eql(tk.trigger, OptionalDouble.class)) {
      return skewedCoercion(tk, field, mapperParam, mapperClass, TypeTool.get().declared(Double.class), tk.trigger);
    }
    if (TypeTool.get().eql(tk.trigger, OptionalLong.class)) {
      return skewedCoercion(tk, field, mapperParam, mapperClass, TypeTool.get().declared(Long.class), tk.trigger);
    }
    return null;
  }

  private Coercion skewedCoercion(
      TriggerKind tk,
      FieldSpec field,
      ParameterSpec mapperParam,
      TypeElement mapperClass,
      TypeMirror baseType) throws TmpException {
    return skewedCoercion(tk, field, mapperParam, mapperClass, baseType, baseType);
  }

  private Coercion skewedCoercion(
      TriggerKind tk,
      FieldSpec field,
      ParameterSpec mapperParam,
      TypeElement mapperClass,
      TypeMirror mapperReturnType,
      TypeMirror baseType) throws TmpException {
    MapperClassValidator.checkReturnType(mapperClass, mapperReturnType);
    CoercionFactory coercionFactory = StandardCoercions.get(baseType);
    if (coercionFactory == null) {
      return null;
    }
    return coercionFactory.getCoercion(field, tk, mapperMap(mapperParam), mapperInit(mapperReturnType, mapperParam, mapperClass.asType()));
  }

  private Coercion handleDefault(
      TriggerKind tk,
      FieldSpec field,
      boolean optional) throws TmpException {
    CoercionFactory enumCoercion = checkEnum(tk.trigger);
    if (enumCoercion != null) {
      return enumCoercion.getCoercion(field, tk);
    } else {
      CoercionFactory factory = StandardCoercions.get(tk.trigger);
      if (factory == null) {
        throw TmpException.findWarning("Bad return type");
      }
      if (factory.handlesOptionalPrimitive() && !optional) {
        throw TmpException.findWarning("Declare this parameter optional.");
      }
      return factory.getCoercion(field, tk);
    }
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
    return CollectorInfo.create(CollectorClassValidator.findInputType(sourceMethod.getReturnType(), collectorClass), collectorClass);
  }

  static String snakeToCamel(String s) {
    StringBuilder sb = new StringBuilder();
    boolean upcase = false;
    boolean underscore = false;
    for (int i = 0; i < s.length(); i++) {
      char c = s.charAt(i);
      if (c == '_') {
        if (underscore) {
          sb.append('_');
        }
        underscore = true;
        upcase = true;
      } else {
        underscore = false;
        if (upcase) {
          sb.append(Character.toUpperCase(c));
          upcase = false;
        } else {
          sb.append(Character.toLowerCase(c));
        }
      }
    }
    return sb.toString();
  }
}
