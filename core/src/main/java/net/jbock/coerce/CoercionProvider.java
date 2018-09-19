package net.jbock.coerce;

import net.jbock.coerce.warn.WarningProvider;
import net.jbock.com.squareup.javapoet.ClassName;
import net.jbock.com.squareup.javapoet.CodeBlock;
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
import java.util.Optional;
import java.util.function.Function;

import static net.jbock.coerce.MapperClassValidator.validateMapperClass;
import static net.jbock.compiler.Constants.STRING;
import static net.jbock.compiler.Util.AS_DECLARED;
import static net.jbock.compiler.Util.QUALIFIED_NAME;

public class CoercionProvider {

  // List and Optional are the only combinators.
  // This knowledge is used in TypeInfo.findParameterizedTypeInfo.
  private static final List<String> COMBINATORS = Arrays.asList(
      "java.util.Optional",
      "java.util.List");

  public static boolean isCombinator(TypeMirror mirror) {
    return COMBINATORS.contains(mirror.accept(QUALIFIED_NAME, null));
  }

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

  public TypeInfo findCoercion(
      ExecutableElement sourceMethod,
      String paramName,
      TypeElement mapperClass) {
    TypeMirror returnType = sourceMethod.getReturnType();
    try {
      CoercionFactory coercion = handle(sourceMethod, paramName, mapperClass);
      return TypeInfo.create(returnType, coercion.getCoercion());
    } catch (TmpException e) {
      String warning = WarningProvider.instance().findWarning(returnType);
      if (warning != null) {
        throw e.asValidationException(sourceMethod, warning);
      }
      throw e.asValidationException(sourceMethod);
    }
  }

  private CoercionFactory handle(
      ExecutableElement sourceMethod,
      String paramName,
      TypeElement mapperClass) throws TmpException {
    if (mapperClass != null && !"java.util.Function".equals(mapperClass.getQualifiedName().toString())) {
      return handleMapperClass(sourceMethod, paramName, mapperClass);
    }
    TypeMirror returnType = sourceMethod.getReturnType();
    if (returnType.getKind() == TypeKind.ARRAY) {
      throw new TmpException("Arrays are not supported. Use List instead.");
    }
    return handleDefault(trigger(returnType));
  }

  private CoercionFactory handleMapperClass(ExecutableElement sourceMethod, String paramName, TypeElement mapperClass) throws TmpException {
    TypeName mapperType = TypeName.get(mapperClass.asType());
    ParameterSpec mapperParam = ParameterSpec.builder(mapperType, snakeToCamel(paramName) + "Mapper").build();
    TypeMirror triggerMirror = trigger(sourceMethod.getReturnType());
    if (triggerMirror.getKind() != TypeKind.DECLARED) {
      throw TmpException.create("Bad return type");
    }
    validateMapperClass(mapperClass, HierarchyUtil.asTypeElement(triggerMirror));
    TypeName trigger = TypeName.get(triggerMirror);

    return new CoercionFactory(trigger) {

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
    };
  }

  private CoercionFactory handleDefault(TypeMirror returnType) throws TmpException {
    Optional<CoercionFactory> enumCoercion = checkEnum(returnType);
    if (enumCoercion.isPresent()) {
      return enumCoercion.get();
    } else {
      if (coercions.get(TypeName.get(returnType)) == null) {
        throw TmpException.create("Bad return type");
      }
      return coercions.get(TypeName.get(returnType));
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

  private TypeMirror trigger(TypeMirror returnType) throws TmpException {
    DeclaredType parameterized = Util.asParameterized(returnType);
    if (parameterized == null) {
      // not a combination, triggered by return type
      return returnType;
    }
    if (!isCombinator(parameterized)) {
      // combinators are the only allowed parameterized types
      throw TmpException.create("Bad return type");
    }
    // combination, triggered by type argument of return type
    TypeMirror typeMirror = parameterized.getTypeArguments().get(0);
    if (typeMirror.getKind() != TypeKind.DECLARED) {
      throw TmpException.create("Bad return type");
    }
    return typeMirror;
  }


  private static String snakeToCamel(String s) {
    StringBuilder sb = new StringBuilder();
    boolean upcase = false;
    for (int i = 0; i < s.length(); i++) {
      char c = s.charAt(i);
      if (c == '_') {
        upcase = true;
      } else if (upcase) {
        sb.append(Character.toUpperCase(c));
        upcase = false;
      } else {
        sb.append(Character.toLowerCase(c));
      }
    }
    return sb.toString();
  }
}
