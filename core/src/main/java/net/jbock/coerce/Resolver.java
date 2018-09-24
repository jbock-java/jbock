package net.jbock.coerce;

import net.jbock.compiler.Util;

import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static java.util.Collections.emptyMap;
import static net.jbock.compiler.HierarchyUtil.getTypeTree;
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

  private Resolver step(Extension extension) {
    TypeElement b = extension.baseClass;
    DeclaredType x = extension.extensionClass;
    Map<Integer, String> newMap = new LinkedHashMap<>();
    Map<String, TypeMirror> newResults = new LinkedHashMap<>(results);
    List<? extends TypeMirror> xParams = x.getTypeArguments();
    List<? extends TypeParameterElement> bParams = b.getTypeParameters();
    for (Entry<Integer, String> entry : names.entrySet()) {
      if (xParams.isEmpty()) {
        // failure: raw type
        return new Resolver(names, emptyMap());
      }
      TypeMirror typeMirror = xParams.get(entry.getKey());
      if (typeMirror.getKind() == TypeKind.TYPEVAR) {
        for (int i = 0; i < bParams.size(); i++) {
          TypeParameterElement bvar = bParams.get(i);
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

  static Resolver resolve(
      String qname,
      Map<Integer, String> map,
      TypeMirror m) {
    List<TypeElement> family = getTypeTree(m);
    Resolver resolver = new Resolver(map, emptyMap());
    Extension extension;
    String tmpname = qname;
    while ((extension = findExtension(family, tmpname)) != null) {
      resolver = resolver.step(extension);
      tmpname = extension.baseClass.getQualifiedName().toString();
    }
    if (!resolver.names.isEmpty() && tmpname.equals(m.accept(QUALIFIED_NAME, null))) {
      DeclaredType declaredType = Util.asParameterized(m);
      if (declaredType != null) {
        Map<String, TypeMirror> results = new LinkedHashMap<>();
        for (Entry<Integer, String> entry : resolver.names.entrySet()) {
          results.put(entry.getValue(), declaredType.getTypeArguments().get(entry.getKey()));
        }
        results.putAll(resolver.results);
        resolver = new Resolver(emptyMap(), results);
      }
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

    @Override
    public String toString() {
      return String.format("%s extends %s", baseClass, extensionClass);
    }
  }

  boolean satisfies(String key, Predicate<TypeMirror> predicate) {
    TypeMirror m = results.get(key);
    return m != null && predicate.test(m);
  }

  TypeMirror get(String key) {
    return results.get(key);
  }

  TypeMirror getOrElseThrow(String key, Supplier<? extends RuntimeException> exception) {
    TypeMirror m = get(key);
    if (m == null) {
      throw exception.get();
    }
    return m;
  }

  @Override
  public String toString() {
    return String.format("%s %s", names, results);
  }
}
