package net.jbock.coerce;

import net.jbock.com.squareup.javapoet.TypeName;
import net.jbock.compiler.Util;
import net.jbock.compiler.ValidationException;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.NestingKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonMap;

final class MapperClassValidator {

  static void validateMapperClass(TypeElement mapperClass, TypeName trigger) throws MapperValidatorException {
    if (mapperClass.getNestingKind() == NestingKind.MEMBER && !mapperClass.getModifiers().contains(Modifier.STATIC)) {
      throw ValidationException.create(mapperClass, "The inner class must be static");
    }
    if (mapperClass.getModifiers().contains(Modifier.PRIVATE)) {
      throw ValidationException.create(mapperClass, "The class may not be private");
    }
    if (!mapperClass.getTypeParameters().isEmpty()) {
      throw ValidationException.create(mapperClass, "The class may not have type parameters");
    }
    List<ExecutableElement> constructors = ElementFilter.constructorsIn(mapperClass.getEnclosedElements());
    if (!constructors.isEmpty()) {
      boolean constructorFound = false;
      for (ExecutableElement constructor : constructors) {
        if (constructor.getParameters().isEmpty()) {
          if (constructor.getModifiers().contains(Modifier.PRIVATE)) {
            throw ValidationException.create(mapperClass, "The class must have a package visible constructor");
          }
          if (!constructor.getThrownTypes().isEmpty()) {
            throw ValidationException.create(mapperClass, "The constructor may not declare any exceptions");
          }
          constructorFound = true;
        }
      }
      if (!constructorFound) {
        throw ValidationException.create(mapperClass, "The class must have a default constructor");
      }
    }
    checkTree(mapperClass, trigger);
  }

  /* Does the mapper implement Supplier<Function<String, triggerClass>>?
   * There can be a situation where this is not very easy. See ProcessorTest.
   */
  private static void checkTree(TypeElement mapperClass, TypeName trigger) throws MapperValidatorException {
    Resolver supplier = Resolver.resolve("java.util.function.Supplier", singletonMap(0, "T"), mapperClass.asType());
    TypeMirror suppliedType = supplier.get("T").orElseThrow(
        () -> MapperValidatorException.create(String.format("The mapper class must implement Supplier<Function<String, %s>>", trigger)));
    Map<Integer, String> functionTypeArgs = new LinkedHashMap<>();
    functionTypeArgs.put(0, "T");
    functionTypeArgs.put(1, "R");
    Resolver function = Resolver.resolve("java.util.function.Function", functionTypeArgs, suppliedType);
    if (!function.satisfies("T", m -> Util.equalsType(m, "java.lang.String")) ||
        !function.satisfies("R", m -> trigger.equals(TypeName.get(m)))) {
      throw MapperValidatorException.create(String.format("The mapper class must implement Supplier<Function<String, %s>>", trigger));
    }
  }

  static class MapperValidatorException extends Exception {
    private MapperValidatorException(String message) {
      super(message);
    }

    private static MapperValidatorException create(String message) {
      return new MapperValidatorException(message);
    }
  }
}
