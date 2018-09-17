package net.jbock.coerce;

import net.jbock.compiler.HierarchyUtil;
import net.jbock.compiler.Util;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.NestingKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import static net.jbock.compiler.HierarchyUtil.asTypeElement;
import static net.jbock.compiler.Util.QUALIFIED_NAME;

final class MapperClassValidator {

  static void validateMapperClass(TypeElement mapperClass, TypeElement trigger) throws TmpException {
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
  private static void checkTree(TypeElement mapperClass, TypeElement trigger) throws TmpException {
    List<TypeElement> family = HierarchyUtil.getFamilyTree(mapperClass.asType());
    Map<Integer, Predicate<TypeElement>> m = new LinkedHashMap<>();
    m.put(0, typeElement -> "java.lang.String".equals(typeElement.getQualifiedName().toString()));
    m.put(1, typeElement -> trigger.getQualifiedName().toString().equals(typeElement.getQualifiedName().toString()));
    ExtensionFunction mask = new ExtensionFunction(m, trigger);
    String qname = "java.util.function.Function";
    while (mask != null) {
      Extension extension = findExtension(family, qname);
      if (extension == null) {
        throw new TmpException(String.format("Mapper class must implement Function<String, %s>", trigger));
      }
      mask = mask.apply(extension);
      qname = extension.baseClass.getQualifiedName().toString();
    }
  }

  private static class ExtensionFunction {

    final Map<Integer, Predicate<TypeElement>> m;
    final TypeElement trigger;

    ExtensionFunction(Map<Integer, Predicate<TypeElement>> m, TypeElement trigger) {
      this.m = m;
      this.trigger = trigger;
    }

    ExtensionFunction apply(Extension extension) throws TmpException {
      TypeElement b = extension.baseClass;
      DeclaredType x = extension.extensionClass;
      Map<Integer, Predicate<TypeElement>> newMap = new LinkedHashMap<>();
      for (Map.Entry<Integer, Predicate<TypeElement>> entry : m.entrySet()) {
        List<? extends TypeMirror> xargs = x.getTypeArguments();
        if (xargs.isEmpty()) {
          // raw type
          throw new TmpException(String.format("Mapper class must implement Function<String, %s>", trigger));
        }
        TypeMirror typeMirror = xargs.get(entry.getKey());
        if (typeMirror.getKind() == TypeKind.TYPEVAR) {
          for (int i = 0; i < b.getTypeParameters().size(); i++) {
            TypeParameterElement bvar = b.getTypeParameters().get(i);
            if (bvar.toString().equals(typeMirror.toString())) {
              newMap.put(i, entry.getValue());
            }
          }
        } else {
          if (!entry.getValue().test(asTypeElement(typeMirror))) {
            throw new TmpException(String.format("Mapper class must implement Function<String, %s>", trigger));
          }
        }
      }
      if (newMap.isEmpty()) {
        return null;
      }
      return new ExtensionFunction(newMap, trigger);
    }
  }

  private static Extension findExtension(List<TypeElement> family, String qname) {
    for (TypeElement element : family) {
      Extension extension = findExtension(element, qname);
      if (extension != null) {
        return extension;
      }
    }
    return null;
  }

  private static Extension findExtension(TypeElement typeElement, String qname) {
    TypeMirror superclass = typeElement.getSuperclass();
    if (superclass != null && qname.equals(superclass.accept(QUALIFIED_NAME, null))) {
      return new Extension(typeElement, superclass.accept(Util.AS_DECLARED, null));
    }
    for (TypeMirror mirror : typeElement.getInterfaces()) {
      if (qname.equals(mirror.accept(QUALIFIED_NAME, null))) {
        return new Extension(typeElement, mirror.accept(Util.AS_DECLARED, null));
      }
    }
    return null;
  }

  private static class Extension {
    final TypeElement baseClass;
    final DeclaredType extensionClass;

    Extension(TypeElement baseClass, DeclaredType extensionClass) {
      this.baseClass = baseClass;
      this.extensionClass = extensionClass;
    }
  }
}
