package net.jbock.coerce;

import net.jbock.compiler.TypeTool;
import net.jbock.compiler.ValidationException;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.NestingKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

final class MapperClassValidator {

  private final ExecutableElement sourceMethod;

  MapperClassValidator(ExecutableElement sourceMethod) {
    this.sourceMethod = sourceMethod;
  }

  TypeMirror validateMapperClass(TypeElement mapperClass) {
    commonChecks(sourceMethod, mapperClass, "mapper");
    return getFunctionReturnType(mapperClass);
  }

  static void commonChecks(ExecutableElement sourceMethod, TypeElement mapperClass, String name) {
    if (mapperClass.getNestingKind() == NestingKind.MEMBER && !mapperClass.getModifiers().contains(Modifier.STATIC)) {
      throw ValidationException.create(sourceMethod,
          String.format("The nested %s class must be static", name));
    }
    if (mapperClass.getModifiers().contains(Modifier.PRIVATE)) {
      throw ValidationException.create(sourceMethod,
          String.format("The %s class may not be private", name));
    }
    List<ExecutableElement> constructors = ElementFilter.constructorsIn(mapperClass.getEnclosedElements());
    if (!constructors.isEmpty()) {
      boolean constructorFound = false;
      for (ExecutableElement constructor : constructors) {
        if (constructor.getParameters().isEmpty()) {
          if (constructor.getModifiers().contains(Modifier.PRIVATE)) {
            throw ValidationException.create(sourceMethod,
                String.format("The %s class must have a package visible constructor", name));
          }
          if (!constructor.getThrownTypes().isEmpty()) {
            throw ValidationException.create(sourceMethod,
                String.format("The %s constructor may not declare any exceptions", name));
          }
          constructorFound = true;
        }
      }
      if (!constructorFound) {
        throw ValidationException.create(sourceMethod,
            String.format("The %s class must have a default constructor", name));
      }
    }
  }

  private TypeMirror getFunctionReturnType(TypeElement supplierClass) {
    Map<String, TypeMirror> supplierTypeargs = Resolver.resolve("java.util.function.Supplier", supplierClass.asType(), "T");
    TypeMirror functionClass = Optional.ofNullable(supplierTypeargs.get("T")).orElseThrow(this::boom);
    Map<String, TypeMirror> functionTypeargs = resolveFunctionTypeargs(functionClass);
    TypeMirror inputType = functionTypeargs.get("T");
    TypeMirror resultType = functionTypeargs.get("R");
    if (inputType == null || resultType == null ||
        !TypeTool.get().equals(inputType, String.class)) {
      throw boom();
    }
    if (resultType.getKind() != TypeKind.DECLARED && resultType.getKind() != TypeKind.ARRAY) {
      throw boom();
    }
    return resultType;
  }

  private Map<String, TypeMirror> resolveFunctionTypeargs(
      TypeMirror functionType) {
    Map<String, TypeMirror> functionTypeargs = Resolver.resolve("java.util.function.Function", functionType, "T", "R");
    TypeTool tool = TypeTool.get();
    DeclaredType string = tool.declared(String.class);
    TypeMirror t = Optional.ofNullable(functionTypeargs.get("T")).orElseThrow(this::boom);
    TypeMirror r = Optional.ofNullable(functionTypeargs.get("R")).orElseThrow(this::boom);
    Map<String, TypeMirror> solution = tool.unify(string, t).orElseThrow(this::boom);
    Map<String, TypeMirror> resolved = new HashMap<>();
    resolved.put("T", string);
    resolved.put("R", tool.substitute(r, solution));
    return resolved;
  }

  final ValidationException boom() {
    return ValidationException.create(sourceMethod, "There is a problem with the mapper class");
  }
}
