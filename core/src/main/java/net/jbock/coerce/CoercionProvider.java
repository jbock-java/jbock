package net.jbock.coerce;

import net.jbock.coerce.warn.WarningProvider;
import net.jbock.com.squareup.javapoet.TypeName;
import net.jbock.compiler.Constants;
import net.jbock.compiler.Util;
import net.jbock.compiler.ValidationException;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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

  private static final List<Coercion> ALL_COERCIONS = Arrays.asList(
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

  private final Map<TypeName, Coercion> coercions;

  private CoercionProvider() {
    coercions = new HashMap<>();
    for (Coercion coercion : ALL_COERCIONS) {
      Coercion previous = this.coercions.put(coercion.trigger(), coercion);
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

  public TypeInfo findCoercion(ExecutableElement sourceMethod) {
    TypeMirror returnType = sourceMethod.getReturnType();
    try {
      if (returnType.getKind() == TypeKind.ARRAY &&
          Util.equalsType(returnType.accept(Util.AS_ARRAY, null).getComponentType(), "java.lang.String")) {
        return TypeInfo.create(returnType, coercions.get(Constants.STRING));
      }
      DeclaredType parameterized = Util.asParameterized(returnType);
      if (parameterized != null) {
        return TypeInfo.create(returnType, findParameterizedCoercion(sourceMethod, parameterized));
      }
      Coercion coercion = find(sourceMethod, returnType);
      return TypeInfo.create(returnType, coercion);
    } catch (TmpException e) {
      String warning = WarningProvider.instance().findWarning(returnType);
      if (warning == null) {
        throw e.asValidationException();
      } else {
        throw e.asValidationException(warning);
      }
    }
  }

  private Coercion find(ExecutableElement sourceMethod, TypeMirror returnType) throws TmpException {
    Optional<Coercion> enumCoercion = checkEnum(returnType);
    if (enumCoercion.isPresent()) {
      return enumCoercion.get();
    } else {
      if (coercions.get(TypeName.get(returnType)) == null) {
        throw TmpException.create(sourceMethod, "Bad return type: " + returnType.accept(QUALIFIED_NAME, null));
      }
      return coercions.get(TypeName.get(returnType));
    }
  }

  private Optional<Coercion> checkEnum(TypeMirror mirror) {
    if (mirror.getKind() != TypeKind.DECLARED) {
      return Optional.empty();
    }
    DeclaredType declared = mirror.accept(AS_DECLARED, null);
    TypeElement element = declared.asElement().accept(Util.AS_TYPE_ELEMENT, null);
    TypeMirror superclass = element.getSuperclass();
    if (!"java.lang.Enum".equals(superclass.accept(QUALIFIED_NAME, null))) {
      return Optional.empty();
    }
    return Optional.of(EnumCoercion.create(TypeName.get(mirror)));
  }

  private Coercion findParameterizedCoercion(
      ExecutableElement sourceMethod,
      DeclaredType parameterized) throws TmpException {
    if (!isCombinator(parameterized)) {
      throw TmpException.create(sourceMethod,
          "Bad return type: " + parameterized.accept(QUALIFIED_NAME, null));
    }
    TypeMirror typeArgument = parameterized.getTypeArguments().get(0);
    Coercion coercion = find(sourceMethod, typeArgument);
    if (coercion.special()) {
      throw TmpException.create(sourceMethod,
          "Bad return type: " + parameterized.accept(QUALIFIED_NAME, null));
    }
    return coercion;
  }

  private static class TmpException extends Exception {
    final ExecutableElement sourceMethod;
    final String message;

    static TmpException create(ExecutableElement sourceMethod, String message) {
      return new TmpException(sourceMethod, message);
    }

    TmpException(ExecutableElement sourceMethod, String message) {
      this.sourceMethod = sourceMethod;
      this.message = message;
    }

    ValidationException asValidationException() {
      return ValidationException.create(sourceMethod, message);
    }

    ValidationException asValidationException(String newMessage) {
      return ValidationException.create(sourceMethod, newMessage);
    }
  }
}
