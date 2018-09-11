package net.jbock.coerce;

import net.jbock.coerce.warn.WarningProvider;
import net.jbock.com.squareup.javapoet.ClassName;
import net.jbock.com.squareup.javapoet.ParameterizedTypeName;
import net.jbock.com.squareup.javapoet.TypeName;
import net.jbock.compiler.Constants;
import net.jbock.compiler.ValidationException;

import javax.lang.model.element.ExecutableElement;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class CoercionProvider {

  // List and Optional are the only combinators.
  // This knowledge is used in TypeInfo.findParameterizedTypeInfo.
  private static final List<ClassName> COMBINATORS = Arrays.asList(
      ClassName.get(Optional.class),
      ClassName.get(List.class));

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
    try {
      TypeName typeName = TypeName.get(sourceMethod.getReturnType());
      if (typeName.equals(Constants.STRING_ARRAY)) {
        return TypeInfo.create(typeName, coercions.get(Constants.STRING));
      }
      if (typeName instanceof ParameterizedTypeName) {
        return TypeInfo.create(typeName, findParameterizedCoercion(sourceMethod, (ParameterizedTypeName) typeName));
      }
      Coercion coercion = coercions.get(typeName);
      if (coercion == null) {
        throw TmpException.create(sourceMethod, "Bad return type: " + typeName);
      }
      return TypeInfo.create(typeName, coercion);
    } catch (TmpException e) {
      String warning = WarningProvider.instance().findWarning(sourceMethod.getReturnType());
      if (warning == null) {
        throw e.asValidationException();
      } else {
        throw e.asValidationException(warning);
      }
    }
  }

  private Coercion findParameterizedCoercion(
      ExecutableElement sourceMethod,
      ParameterizedTypeName typeName) throws TmpException {
    ClassName rawType = typeName.rawType;
    if (!COMBINATORS.contains(rawType)) {
      throw TmpException.create(sourceMethod, "Bad return type: " + typeName);
    }
    TypeName typeArgument = typeName.typeArguments.get(0);
    Coercion coercion = coercions.get(typeArgument);
    if (coercion.special()) {
      throw TmpException.create(sourceMethod, "Bad return type: " + typeName);
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
