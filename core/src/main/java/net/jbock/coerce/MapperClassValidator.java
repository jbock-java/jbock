package net.jbock.coerce;

import net.jbock.compiler.TypeTool;
import net.jbock.compiler.ValidationException;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.NestingKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Collections.singletonMap;

final class MapperClassValidator {

  static void validateMapperClass(TypeElement mapperClass, TypeMirror trigger) throws MapperValidatorException {
    commonChecks(mapperClass, "mapper");
    checkTree(mapperClass, trigger);
  }

  static void commonChecks(TypeElement mapperClass, String name) {
    if (mapperClass.getNestingKind() == NestingKind.MEMBER && !mapperClass.getModifiers().contains(Modifier.STATIC)) {
      throw ValidationException.create(mapperClass,
          String.format("The nested %s class must be static", name));
    }
    if (mapperClass.getModifiers().contains(Modifier.PRIVATE)) {
      throw ValidationException.create(mapperClass,
          String.format("The %s class may not be private", name));
    }
    List<ExecutableElement> constructors = ElementFilter.constructorsIn(mapperClass.getEnclosedElements());
    if (!constructors.isEmpty()) {
      boolean constructorFound = false;
      for (ExecutableElement constructor : constructors) {
        if (constructor.getParameters().isEmpty()) {
          if (constructor.getModifiers().contains(Modifier.PRIVATE)) {
            throw ValidationException.create(mapperClass,
                String.format("The %s class must have a package visible constructor", name));
          }
          if (!constructor.getThrownTypes().isEmpty()) {
            throw ValidationException.create(mapperClass,
                String.format("The %s constructor may not declare any exceptions", name));
          }
          constructorFound = true;
        }
      }
      if (!constructorFound) {
        throw ValidationException.create(mapperClass,
            String.format("The %s class must have a default constructor", name));
      }
    }
  }

  /* Does the mapper implement Supplier<Function<String, triggerClass>>?
   * There can be a situation where this is not very easy. See ProcessorTest.
   */
  private static TypeMirror checkTree(TypeElement mapperClass, TypeMirror trigger) throws MapperValidatorException {
    Resolver supplier = Resolver.resolve("java.util.function.Supplier", singletonMap(0, "T"), mapperClass.asType());
    TypeMirror suppliedType = supplier.get("T").orElseThrow(
        () -> MapperValidatorException.create(String.format("The mapper class must implement Supplier<Function<String, %s>>", trigger)));
    Map<String, TypeMirror> resolved = getResolved(mapperClass, trigger, suppliedType);
    TypeMirror inputType = resolved.get("T");
    TypeMirror resultType = resolved.get("R");
    if (inputType == null || resultType == null ||
        !TypeTool.get().equals(inputType, String.class) ||
        !TypeTool.get().equals(resultType, trigger)) {
      throw MapperValidatorException.create(String.format("The mapper class must implement Supplier<Function<String, %s>>", trigger));
    }
    return resultType;
  }

  private static Map<String, TypeMirror> getResolved(
      TypeElement mapperClass,
      TypeMirror trigger,
      TypeMirror suppliedType) throws MapperValidatorException {
    Map<Integer, String> functionTypeArgs = new LinkedHashMap<>();
    functionTypeArgs.put(0, "T");
    functionTypeArgs.put(1, "R");
    Resolver resolver = Resolver.resolve("java.util.function.Function", functionTypeArgs, suppliedType);
    Map<String, TypeMirror> resolved;
    if (!mapperClass.getTypeParameters().isEmpty()) {
      Optional<Map<String, DeclaredType>> solution = TypeTool.get().unify(TypeTool.get().declared(String.class), resolver.get("T").orElseThrow(
          () -> MapperValidatorException.create(String.format("The mapper class must implement Supplier<Function<String, %s>>", trigger))));
      if (!solution.isPresent()) {
        throw MapperValidatorException.create(String.format("The mapper class must implement Supplier<Function<String, %s>>", trigger));
      }
      resolved = new HashMap<>();
      resolved.put("R", TypeTool.get().substitute(resolver.get("R")
              .orElseThrow(() -> MapperValidatorException.create(String.format("The mapper class must implement Supplier<Function<String, %s>>", trigger))),
          solution.get()));
      resolved.put("T", TypeTool.get().substitute(resolver.get("T")
              .orElseThrow(() -> MapperValidatorException.create(String.format("The mapper class must implement Supplier<Function<String, %s>>", trigger))),
          solution.get()));
    } else {
      resolved = resolver.asMap();
    }
    return resolved;
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
