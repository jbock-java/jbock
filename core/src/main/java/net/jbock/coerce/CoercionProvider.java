package net.jbock.coerce;

import net.jbock.coerce.warn.WarningProvider;
import net.jbock.com.squareup.javapoet.FieldSpec;
import net.jbock.com.squareup.javapoet.ParameterSpec;
import net.jbock.com.squareup.javapoet.TypeName;
import net.jbock.compiler.Constants;
import net.jbock.compiler.Util;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static javax.lang.model.element.Modifier.FINAL;
import static net.jbock.coerce.CoercionKind.SIMPLE;
import static net.jbock.coerce.CoercionKind.findKind;
import static net.jbock.coerce.MapperClassValidator.validateMapperClass;
import static net.jbock.coerce.MapperCoercion.mapperInit;
import static net.jbock.coerce.MapperCoercion.mapperMap;
import static net.jbock.compiler.Util.AS_DECLARED;
import static net.jbock.compiler.Util.QUALIFIED_NAME;

public class CoercionProvider {

  private static final List<CoercionFactory> ALL_COERCIONS = Arrays.asList(
      new CharsetCoercion(),
      new PatternCoercion(),
      new ObjectIntegerCoercion(),
      new PrimitiveIntCoercion(),
      new OptionalIntCoercion(),
      new ObjectLongCoercion(),
      new PrimitiveLongCoercion(),
      new OptionalDoubleCoercion(),
      new ObjectDoubleCoercion(),
      new PrimitiveDoubleCoercion(),
      new ObjectFloatCoercion(),
      new PrimitiveFloatCoercion(),
      new OptionalLongCoercion(),
      new ObjectCharacterCoercion(),
      new PrimitiveCharacterCoercion(),
      new ObjectBooleanCoercion(),
      new PrimitiveBooleanCoercion(),
      new ObjectShortCoercion(),
      new PrimitiveShortCoercion(),
      new ObjectByteCoercion(),
      new PrimitiveByteCoercion(),
      new PathCoercion(),
      new FileCoercion(),
      new URICoercion(),
      new BigDecimalCoercion(),
      new BigIntegerCoercion(),
      new LocalDateCoercion(),
      new LocalDateTimeCoercion(),
      new OffsetDateTimeCoercion(),
      new ZonedDateTimeCoercion(),
      new InstantCoercion(),
      new StringCoercion());

  private static CoercionProvider instance;

  private final Map<TypeName, CoercionFactory> coercions;

  private CoercionProvider() {
    coercions = new HashMap<>();
    for (CoercionFactory coercion : ALL_COERCIONS) {
      CoercionFactory previous = this.coercions.put(coercion.trigger(), coercion);
      if (previous != null) {
        throw new IllegalStateException(String.format("Both triggered by %s : %s, %s",
            coercion.trigger(),
            coercion.getClass().getSimpleName(), previous.getClass().getSimpleName()));
      }
    }
  }

  public static CoercionProvider getInstance() {
    if (instance == null) {
      instance = new CoercionProvider();
    }
    return instance;
  }

  public Coercion findCoercion(
      ExecutableElement sourceMethod,
      String paramName,
      TypeElement mapperClass) {
    TypeMirror returnType = sourceMethod.getReturnType();
    try {
      return handle(sourceMethod, paramName, mapperClass);
    } catch (TmpException e) {
      String warning = WarningProvider.instance().findWarning(returnType);
      if (warning != null) {
        throw e.asValidationException(sourceMethod, warning);
      }
      throw e.asValidationException(sourceMethod);
    }
  }

  private Coercion handle(
      ExecutableElement sourceMethod,
      String paramName,
      TypeElement mapperClass) throws TmpException {
    FieldSpec field = FieldSpec.builder(TypeName.get(sourceMethod.getReturnType()),
        snakeToCamel(paramName))
        .addModifiers(FINAL)
        .build();
    if (mapperClass != null && !"java.util.Function".equals(mapperClass.getQualifiedName().toString())) {
      return handleMapper(sourceMethod, paramName, mapperClass, field);
    }
    TypeMirror returnType = sourceMethod.getReturnType();
    if (returnType.getKind() == TypeKind.ARRAY) {
      throw new TmpException("Arrays are not supported. Use List instead.");
    }
    TriggerKind tk = trigger(returnType);
    if (Util.asParameterized(tk.trigger) != null) {
      // wrapped type can't have type arguments
      throw TmpException.create("Bad return type");
    }
    return handleDefault(tk, field);
  }

  private Coercion handleMapper(
      ExecutableElement sourceMethod,
      String paramName,
      TypeElement mapperClass,
      FieldSpec field) throws TmpException {
    TypeName mapperType = TypeName.get(mapperClass.asType());
    ParameterSpec mapperParam = ParameterSpec.builder(mapperType, snakeToCamel(paramName) + "Mapper").build();
    TriggerKind tk = trigger(sourceMethod.getReturnType());
    MapperSkew skew = mapperSkew(tk);
    if (skew != null) {
      validateMapperClass(mapperClass, skew.mapperReturnType);
      return coercions.get(skew.baseType).getCoercion(field, tk.kind)
          .withMapper(mapperMap(mapperParam), mapperInit(skew.mapperReturnType, mapperParam, mapperType));
    } else {
      validateMapperClass(mapperClass, TypeName.get(tk.trigger));
      return MapperCoercion.create(tk, mapperParam, mapperType, field);
    }
  }

  private MapperSkew mapperSkew(TriggerKind tk) {
    if (tk.kind != SIMPLE) {
      return null;
    }
    TypeName input = TypeName.get(tk.trigger);
    if (input.isPrimitive()) {
      if (!coercions.containsKey(input)) {
        return null;
      }
      return new MapperSkew(input.box(), input.box());
    }
    if (input.equals(Constants.OPTIONAL_INT)) {
      return new MapperSkew(TypeName.get(Integer.class), Constants.OPTIONAL_INT);
    } else if (input.equals(Constants.OPTIONAL_DOUBLE)) {
      return new MapperSkew(TypeName.get(Double.class), Constants.OPTIONAL_DOUBLE);
    } else if (input.equals(Constants.OPTIONAL_LONG)) {
      return new MapperSkew(TypeName.get(Long.class), Constants.OPTIONAL_LONG);
    }
    return null;
  }

  private Coercion handleDefault(
      TriggerKind tk,
      FieldSpec field) throws TmpException {
    CoercionFactory enumCoercion = checkEnum(tk.trigger);
    if (enumCoercion != null) {
      return enumCoercion.getCoercion(field, tk.kind);
    } else {
      CoercionFactory factory = coercions.get(TypeName.get(tk.trigger));
      if (factory == null) {
        throw TmpException.create("Bad return type");
      }
      return factory.getCoercion(field, tk.kind);
    }
  }

  private CoercionFactory checkEnum(TypeMirror mirror) throws TmpException {
    if (mirror.getKind() != TypeKind.DECLARED) {
      return null;
    }
    DeclaredType declared = mirror.accept(AS_DECLARED, null);
    TypeElement element = declared.asElement().accept(Util.AS_TYPE_ELEMENT, null);
    TypeMirror superclass = element.getSuperclass();
    if (!"java.lang.Enum".equals(superclass.accept(QUALIFIED_NAME, null))) {
      return null;
    }
    if (element.getModifiers().contains(Modifier.PRIVATE)) {
      throw TmpException.create("Private return type is not allowed");
    }
    return EnumCoercion.create(TypeName.get(mirror));
  }

  private TriggerKind trigger(TypeMirror returnType) throws TmpException {
    DeclaredType parameterized = Util.asParameterized(returnType);
    if (parameterized == null) {
      // not a combination, triggered by return type
      return CoercionKind.SIMPLE.of(returnType);
    }
    CoercionKind kind = findKind(parameterized);
    if (!kind.isCombination()) {
      throw TmpException.create("Bad return type");
    }
    return kind.of(parameterized.getTypeArguments().get(0));
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
