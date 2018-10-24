package net.jbock.coerce;

import net.jbock.compiler.TypeTool;

import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import static net.jbock.compiler.HierarchyUtil.getTypeTree;

class Resolver {

  private final Map<Integer, String> names;
  private final Map<String, TypeMirror> results = new HashMap<>();
  private final List<Extension> extensions = new ArrayList<>();

  private Resolver(Map<Integer, String> names) {
    this.names = names;
  }

  void setResults(Map<String, TypeMirror> results) {
    this.results.clear();
    this.results.putAll(results);
  }

  void setNames(Map<Integer, String> names) {
    this.names.clear();
    this.names.putAll(names);
  }

  void clearResults() {
    this.results.clear();
  }

  void clearNames() {
    this.names.clear();
  }

  private void step(Extension extension) {
    TypeElement b = extension.baseClass();
    DeclaredType x = extension.extensionClass();
    Map<Integer, String> newNames = new LinkedHashMap<>();
    Map<String, TypeMirror> newResults = new LinkedHashMap<>(results);
    List<? extends TypeMirror> xParams = x.getTypeArguments();
    List<? extends TypeParameterElement> bParams = b.getTypeParameters();
    for (Entry<Integer, String> entry : names.entrySet()) {
      if (xParams.isEmpty()) {
        // failure: raw type
        results.clear();
        return;
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
    setResults(newResults);
    setNames(newNames);
  }

  static Resolver resolve(TypeMirror goal, TypeMirror start, String... typevars) {
    Map<Integer, String> map = new HashMap<>();
    for (int i = 0; i < typevars.length; i++) {
      String typevar = typevars[i];
      map.put(i, typevar);
    }
    return resolve(goal, start, map);
  }

  private static Resolver resolve(
      TypeMirror goal,
      TypeMirror start,
      Map<Integer, String> names) {
    List<TypeElement> family = getTypeTree(start);
    Resolver resolver = new Resolver(names);
    Extension extension;
    TypeTool tool = TypeTool.get();
    TypeMirror nextGoal = tool.erasure(goal);
    while ((extension = findExtension(family, nextGoal)) != null) {
      resolver.step(extension);
      resolver.extensions.add(extension);
      nextGoal = tool.erasure(extension.baseClass().asType());
    }
    if (resolver.names.isEmpty()) {
      // everything resolved
      return resolver;
    }
    if (tool.eql(nextGoal, tool.erasure(start))) {
      List<? extends TypeMirror> typeargs = tool.typeargs(start);
      Map<String, TypeMirror> results = new LinkedHashMap<>();
      for (Entry<Integer, String> entry : resolver.names.entrySet()) {
        if (entry.getKey() < typeargs.size()) {
          results.put(entry.getValue(), typeargs.get(entry.getKey()));
        }
      }
      results.putAll(resolver.results);
      resolver.clearNames();
      resolver.setResults(results);
    }
    return resolver;
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
    if (superclass != null && tool.eql(qname, tool.erasure(superclass))) {
      return new Extension(typeElement, tool.asDeclared(superclass));
    }
    for (TypeMirror mirror : typeElement.getInterfaces()) {
      if (tool.eql(qname, tool.erasure(mirror))) {
        return new Extension(typeElement, tool.asDeclared(mirror));
      }
    }
    return null;
  }

  Map<String, TypeMirror> asMap() {
    return Collections.unmodifiableMap(results);
  }

  List<Extension> extensions() {
    List<Extension> copy = new ArrayList<>(this.extensions);
    Collections.reverse(copy);
    return Collections.unmodifiableList(copy);
  }

  @Override
  public String toString() {
    return String.format("%s %s", names, results);
  }
}
