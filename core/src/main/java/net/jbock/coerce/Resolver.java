package net.jbock.coerce;

import net.jbock.compiler.HierarchyUtil;
import net.jbock.compiler.Util;

import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import static net.jbock.compiler.Util.QUALIFIED_NAME;

class Resolver {

  private final Map<Integer, String> names;
  private final Map<String, TypeMirror> results;

  private Resolver(
      Map<Integer, String> names,
      Map<String, TypeMirror> results) {
    this.names = names;
    this.results = results;
  }

  private Resolver step(Extension extension) throws TmpException {
    TypeElement b = extension.baseClass;
    DeclaredType x = extension.extensionClass;
    Map<Integer, String> newMap = new LinkedHashMap<>();
    Map<String, TypeMirror> newResults = new LinkedHashMap<>(results);
    for (Map.Entry<Integer, String> entry : names.entrySet()) {
      List<? extends TypeMirror> xargs = x.getTypeArguments();
      if (xargs.isEmpty()) {
        // raw type
        throw new TmpException("Raw types are not allowed");
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
        newResults.put(entry.getValue(), typeMirror);
      }
    }
    return new Resolver(newMap, newResults);
  }

  boolean satisfies(String key, Predicate<TypeMirror> predicate) {
    TypeMirror m = results.get(key);
    return m != null && predicate.test(m);
  }

  static Resolver resolve(
      String qname,
      Map<Integer, String> map,
      TypeElement mapperClass) throws TmpException {
    List<TypeElement> family = HierarchyUtil.getFamilyTree(mapperClass.asType());
    Resolver resolver = new Resolver(map, Collections.emptyMap());
    Extension extension;
    while ((extension = findExtension(family, qname)) != null) {
      resolver = resolver.step(extension);
      qname = extension.baseClass.getQualifiedName().toString();
    }
    return resolver;
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
