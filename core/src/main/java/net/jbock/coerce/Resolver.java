package net.jbock.coerce;

import net.jbock.compiler.TypeTool;

import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.TypeMirror;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static net.jbock.compiler.HierarchyUtil.getTypeTree;
import static net.jbock.compiler.TypeTool.asDeclared;

class Resolver {

  private final List<Extension> extensions;

  private Resolver(List<Extension> extensions) {
    List<Extension> reversed = new ArrayList<>(extensions);
    Collections.reverse(reversed);
    this.extensions = Collections.unmodifiableList(reversed);
  }

  static Resolver resolve(
      TypeMirror goal,
      TypeMirror start) {
    List<TypeElement> family = getTypeTree(start);
    Extension extension;
    TypeTool tool = TypeTool.get();
    TypeMirror nextGoal = tool.erasure(goal);
    List<Extension> extensions = new ArrayList<>();
    while ((extension = findExtension(family, nextGoal)) != null) {
      extensions.add(extension);
      nextGoal = tool.erasure(extension.baseClass().asType());
    }
    return new Resolver(extensions);
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
      return new Extension(typeElement, asDeclared(superclass));
    }
    for (TypeMirror mirror : typeElement.getInterfaces()) {
      if (tool.eql(qname, tool.erasure(mirror))) {
        return new Extension(typeElement, asDeclared(mirror));
      }
    }
    return null;
  }

  Optional<TypeMirror> resolveTypevars() {
    if (extensions.isEmpty()) {
      return Optional.empty();
    }
    TypeMirror x = extensions.get(0).extensionClass();
    for (int i = 1; i < extensions.size(); i++) {
      Extension extension = extensions.get(i);
      x = resolveStep(x, extension);
    }
    return Optional.of(x);
  }

  private static TypeMirror resolveStep(TypeMirror x, Extension ex1) {
    List<? extends TypeMirror> typeArguments = TypeTool.get().asDeclared(x).getTypeArguments();
    List<? extends TypeParameterElement> typeParameters = ex1.baseClass().getTypeParameters();
    Map<String, TypeMirror> resolution = new HashMap<>();
    for (int i = 0; i < typeParameters.size(); i++) {
      resolution.put(typeParameters.get(i).toString(), typeArguments.get(i));
    }
    return TypeTool.get().substitute(ex1.extensionClass(), resolution).orElse(ex1.extensionClass());
  }

  @Override
  public String toString() {
    return extensions.toString();
  }
}
