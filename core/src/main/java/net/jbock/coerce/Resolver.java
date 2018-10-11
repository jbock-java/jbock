package net.jbock.coerce;

import net.jbock.compiler.TypeTool;
import net.jbock.compiler.Util;

import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import static java.util.Collections.emptyMap;
import static net.jbock.compiler.HierarchyUtil.getTypeTree;

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
    Map<Integer, String> newNames = new LinkedHashMap<>();
    Map<String, TypeMirror> newResults = new LinkedHashMap<>(results);
    List<? extends TypeMirror> xParams = x.getTypeArguments();
    List<? extends TypeParameterElement> bParams = b.getTypeParameters();
    for (Entry<Integer, String> entry : names.entrySet()) {
      if (xParams.isEmpty()) {
        // failure: raw type
        return new Resolver(names, emptyMap());
      }
      TypeMirror xParam = xParams.get(entry.getKey());
      if (xParam.getKind() == TypeKind.TYPEVAR) {
        for (int i = 0; i < bParams.size(); i++) {
          TypeParameterElement bParam = bParams.get(i);
          if (bParam.toString().equals(xParam.toString())) {
            newNames.put(i, entry.getValue());
            break;
          }
        }
      } else {
        newResults.put(entry.getValue(), xParam);
      }
    }
    return new Resolver(newNames, newResults);
  }

  static Map<String, TypeMirror> resolve(TypeMirror qname, TypeMirror m, String... typevars) {
    Map<Integer, String> map = new HashMap<>();
    for (int i = 0; i < typevars.length; i++) {
      String typevar = typevars[i];
      map.put(i, typevar);
    }
    return resolve(qname, map, m);
  }

  private static Map<String, TypeMirror> resolve(
      TypeMirror qname,
      Map<Integer, String> map,
      TypeMirror m) {
    List<TypeElement> family = getTypeTree(m);
    Resolver resolver = new Resolver(map, emptyMap());
    Extension extension;
    TypeTool tool = TypeTool.get();
    TypeMirror tmpname = tool.erasure(qname);
    while ((extension = findExtension(family, tmpname)) != null) {
      resolver = resolver.step(extension);
      tmpname = tool.erasure(extension.baseClass.asType());
    }
    if (resolver.names.isEmpty()) {
      // everything resolved
      return resolver.asMap();
    }
    if (tool.equals(tmpname, tool.erasure(m))) {
      List<? extends TypeMirror> typeargs = m.accept(TypeTool.TYPEARGS, null);
      Map<String, TypeMirror> results = new LinkedHashMap<>();
      for (Entry<Integer, String> entry : resolver.names.entrySet()) {
        if (entry.getKey() < typeargs.size()) {
          results.put(entry.getValue(), typeargs.get(entry.getKey()));
        }
      }
      results.putAll(resolver.results);
      resolver = new Resolver(emptyMap(), results);
    }
    return resolver.asMap();
  }

  private static Extension findExtension(List<TypeElement> family, TypeMirror qname) {
    for (TypeElement element : family) {
      Extension extension = findExtension(element, qname);
      if (extension != null) {
        return extension;
      }
    }
    return null;
  }

  private static Extension findExtension(TypeElement typeElement, TypeMirror qname) {
    TypeTool tool = TypeTool.get();
    TypeMirror superclass = typeElement.getSuperclass();
    if (superclass != null && tool.equals(qname, tool.erasure(superclass))) {
      return new Extension(typeElement, superclass.accept(Util.AS_DECLARED, null));
    }
    for (TypeMirror mirror : typeElement.getInterfaces()) {
      if (tool.equals(qname, tool.erasure(mirror))) {
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

  private Map<String, TypeMirror> asMap() {
    return Collections.unmodifiableMap(results);
  }

  @Override
  public String toString() {
    return String.format("%s %s", names, results);
  }
}
