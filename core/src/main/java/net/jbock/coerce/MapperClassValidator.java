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
import java.util.List;
import java.util.Map;
import java.util.Optional;

final class MapperClassValidator {

  static TypeMirror validateMapperClass(TypeElement mapperClass) throws MapEx {
    commonChecks(mapperClass, "mapper");
    return getFunctionReturnType(mapperClass);
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

  private static TypeMirror getFunctionReturnType(TypeElement mapperClass) throws MapEx {
    Map<String, TypeMirror> supplierTypeargs = Resolver.resolve("java.util.function.Supplier", mapperClass.asType(), "T");
    TypeMirror suppliedType = Optional.ofNullable(supplierTypeargs.get("T")).orElseThrow(MapEx::boom);
    Map<String, TypeMirror> functionTypeargs = resolveFunctionTypeargs(mapperClass, suppliedType);
    TypeMirror inputType = functionTypeargs.get("T");
    TypeMirror resultType = functionTypeargs.get("R");
    if (inputType == null || resultType == null ||
        !TypeTool.get().equals(inputType, String.class)) {
      throw new MapEx();
    }
    return resultType;
  }

  private static Map<String, TypeMirror> resolveFunctionTypeargs(
      TypeElement mapperClass,
      TypeMirror functionType) throws MapEx {
    Map<String, TypeMirror> functionTypeargs = Resolver.resolve("java.util.function.Function", functionType, "T", "R");
    if (mapperClass.getTypeParameters().isEmpty()) {
      return functionTypeargs;
    }
    TypeTool tool = TypeTool.get();
    DeclaredType string = tool.declared(String.class);
    TypeMirror t = Optional.ofNullable(functionTypeargs.get("T")).orElseThrow(MapEx::boom);
    TypeMirror r = Optional.ofNullable(functionTypeargs.get("R")).orElseThrow(MapEx::boom);
    Map<String, TypeMirror> resolved = new HashMap<>();
    resolved.put("T", string);
    resolved.put("R", tool.substitute(r, tool.unify(string, t).orElseThrow(MapEx::new)));
    return resolved;
  }

  static class MapEx extends Exception {
    static MapEx boom() {
      return new MapEx();
    }
  }
}
