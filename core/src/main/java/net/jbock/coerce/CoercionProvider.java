package net.jbock.coerce;

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

  private static final List<ClassName> COMBINATORS = Arrays.asList(
      ClassName.get(Optional.class),
      ClassName.get(List.class));

  private static final List<Coercion> ALL_COERCIONS = Arrays.asList(
      new StringCoercion(),
      new IntegerCoercion(),
      new IntCoercion(),
      new BooleanObjectCoercion(),
      new BooleanPrimitiveCoercion(),
      new OptionalIntCoercion());

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

  public Coercion findCoercion(ExecutableElement sourceMethod) {
    TypeName typeName = TypeName.get(sourceMethod.getReturnType());
    if (typeName.equals(Constants.STRING_ARRAY)) {
      return coercions.get(Constants.STRING);
    }
    if (typeName instanceof ParameterizedTypeName) {
      return findParameterizedCoercion(sourceMethod, (ParameterizedTypeName) typeName);
    }
    Coercion coercion = coercions.get(typeName);
    if (coercion == null) {
      throw ValidationException.create(sourceMethod, "Bad return type: " + typeName);
    }
    return coercion;
  }

  private Coercion findParameterizedCoercion(
      ExecutableElement sourceMethod,
      ParameterizedTypeName parameterizedType) {
    ClassName rawType = parameterizedType.rawType;
    if (!COMBINATORS.contains(rawType)) {
      throw ValidationException.create(sourceMethod, "Bad return type" + parameterizedType);
    }
    TypeName typeArgument = parameterizedType.typeArguments.get(0);
    Coercion coercion = coercions.get(typeArgument);
    if (coercion.special()) {
      throw ValidationException.create(sourceMethod, "Bad return type" + parameterizedType);
    }
    return coercion;
  }
}
