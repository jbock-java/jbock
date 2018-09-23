package net.jbock.coerce;

import net.jbock.com.squareup.javapoet.TypeName;
import net.jbock.compiler.Util;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.NestingKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class MapperClassValidator {

  static void validateMapperClass(TypeElement mapperClass, TypeName trigger) throws TmpException {
    if (mapperClass.getNestingKind() == NestingKind.MEMBER && !mapperClass.getModifiers().contains(Modifier.STATIC)) {
      throw new TmpException("Inner class " + mapperClass + " must be static");
    }
    if (mapperClass.getModifiers().contains(Modifier.PRIVATE)) {
      throw new TmpException("Mapper class " + mapperClass + " must not be private");
    }
    if (!mapperClass.getTypeParameters().isEmpty()) {
      throw new TmpException("Mapper class " + mapperClass + " must not have type parameters");
    }
    List<ExecutableElement> constructors = ElementFilter.constructorsIn(mapperClass.getEnclosedElements());
    if (!constructors.isEmpty()) {
      boolean constructorFound = false;
      for (ExecutableElement constructor : constructors) {
        if (constructor.getParameters().isEmpty()) {
          if (constructor.getModifiers().contains(Modifier.PRIVATE)) {
            throw new TmpException("Mapper class " + mapperClass + " must have a package visible constructor");
          }
          if (!constructor.getThrownTypes().isEmpty()) {
            throw new TmpException("The constructor of mapper class " + mapperClass + " may not declare any exceptions");
          }
          constructorFound = true;
        }
      }
      if (!constructorFound) {
        throw new TmpException("Mapper class " + mapperClass + " must have a default constructor");
      }
    }
    checkTree(mapperClass, trigger);
  }

  /* Does the mapper implement Function<String, triggerClass>?
   * There can be a situation where this is not very easy. See ProcessorTest.
   */
  private static void checkTree(TypeElement mapperClass, TypeName trigger) throws TmpException {
    Map<Integer, String> map = new LinkedHashMap<>();
    map.put(0, "T");
    map.put(1, "R");
    Resolver resolver = Resolver.resolve("java.util.function.Function", map, mapperClass);
    if (!resolver.satisfies("T", m -> Util.equalsType(m, "java.lang.String")) ||
        !resolver.satisfies("R", m -> trigger.equals(TypeName.get(m)))) {
      throw new TmpException(String.format("Mapper class must implement Function<String, %s>", trigger));
    }
  }
}
