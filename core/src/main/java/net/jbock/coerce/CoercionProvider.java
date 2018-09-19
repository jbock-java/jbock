package net.jbock.coerce;

import net.jbock.coerce.warn.WarningProvider;
import net.jbock.com.squareup.javapoet.ClassName;
import net.jbock.com.squareup.javapoet.CodeBlock;
import net.jbock.com.squareup.javapoet.FieldSpec;
import net.jbock.com.squareup.javapoet.ParameterSpec;
import net.jbock.com.squareup.javapoet.ParameterizedTypeName;
import net.jbock.com.squareup.javapoet.TypeName;
import net.jbock.compiler.HierarchyUtil;
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
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Function;

import static javax.lang.model.element.Modifier.FINAL;
import static net.jbock.coerce.CoercionKind.findKind;
import static net.jbock.coerce.MapperClassValidator.validateMapperClass;
import static net.jbock.compiler.Constants.STRING;
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
      return handleMapperClass(sourceMethod, paramName, mapperClass, field);
    }
    TypeMirror returnType = sourceMethod.getReturnType();
    if (returnType.getKind() == TypeKind.ARRAY) {
      throw new TmpException("Arrays are not supported. Use List instead.");
    }
    return handleDefault(trigger(returnType), field);
  }

  private Coercion handleMapperClass(
      ExecutableElement sourceMethod, String paramName, TypeElement mapperClass, FieldSpec field) throws TmpException {
    TypeName mapperType = TypeName.get(mapperClass.asType());
    ParameterSpec mapperParam = ParameterSpec.builder(mapperType, snakeToCamel(paramName) + "Mapper").build();
    Entry<CoercionKind, TypeMirror> triggerKind = trigger(sourceMethod.getReturnType());
    final TypeMirror trigger = triggerKind.getValue();
    if (trigger.getKind() != TypeKind.DECLARED) {
      throw TmpException.create("Bad return type");
    }
    validateMapperClass(mapperClass, HierarchyUtil.asTypeElement(trigger));

    return new CoercionFactory(TypeName.get(trigger)) {

      @Override
      public CodeBlock map() {
        return CodeBlock.builder().add(".map($N)", mapperParam).build();
      }

      @Override
      public Optional<CodeBlock> initMapper() {
        CodeBlock codeBlock = CodeBlock.builder()
            .add("$T $N = new $T()", ParameterizedTypeName.get(ClassName.get(Function.class), STRING, trigger), mapperParam, mapperType)
            .build();
        return Optional.of(codeBlock);
      }
    }.getCoercion(field, triggerKind.getKey());
  }

  private Coercion handleDefault(
      Entry<CoercionKind, TypeMirror> triggerKind,
      FieldSpec field) throws TmpException {
    TypeMirror trigger = triggerKind.getValue();
    CoercionKind kind = triggerKind.getKey();
    Optional<CoercionFactory> enumCoercion = checkEnum(trigger);
    if (enumCoercion.isPresent()) {
      return enumCoercion.get().getCoercion(field, kind);
    } else {
      if (coercions.get(TypeName.get(trigger)) == null) {
        throw TmpException.create("Bad return type");
      }
      return coercions.get(TypeName.get(trigger)).getCoercion(field, kind);
    }
  }

  private Optional<CoercionFactory> checkEnum(TypeMirror mirror) throws TmpException {
    if (mirror.getKind() != TypeKind.DECLARED) {
      return Optional.empty();
    }
    DeclaredType declared = mirror.accept(AS_DECLARED, null);
    TypeElement element = declared.asElement().accept(Util.AS_TYPE_ELEMENT, null);
    TypeMirror superclass = element.getSuperclass();
    if (!"java.lang.Enum".equals(superclass.accept(QUALIFIED_NAME, null))) {
      return Optional.empty();
    }
    if (element.getModifiers().contains(Modifier.PRIVATE)) {
      throw TmpException.create("Private return type is not allowed");
    }
    return Optional.of(EnumCoercion.create(TypeName.get(mirror)));
  }

  private Entry<CoercionKind, TypeMirror> trigger(TypeMirror returnType) throws TmpException {
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
