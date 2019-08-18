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
  private final TypeTool tool;

  private Resolver(
      List<Extension> extensions,
      TypeTool tool) {
    List<Extension> reversed = new ArrayList<>(extensions);
    Collections.reverse(reversed);
    this.extensions = Collections.unmodifiableList(reversed);
    this.tool = tool;
  }

  static Resolver resolve(TypeMirror goal, TypeMirror start, TypeTool tool) {
    List<TypeElement> family = getTypeTree(start, tool);
    Extension extension;
    TypeMirror nextGoal = tool.erasure(goal);
    List<Extension> extensions = new ArrayList<>();
    while ((extension = findExtension(family, nextGoal, tool)) != null) {
      extensions.add(extension);
      nextGoal = tool.erasure(extension.baseClass().asType());
    }
    return new Resolver(extensions, tool);
  }

  private static Extension findExtension(List<TypeElement> family, TypeMirror goal, TypeTool tool) {
    for (TypeElement element : family) {
      Extension extension = findExtension(element, goal, tool);
      if (extension != null) {
        return extension;
      }
    }
    return null;
  }

  private static Extension findExtension(TypeElement typeElement, TypeMirror goal, TypeTool tool) {
    TypeMirror superclass = typeElement.getSuperclass();
    if (superclass != null && tool.isSameType(goal, tool.erasure(superclass))) {
      return new Extension(typeElement, asDeclared(superclass));
    }
    for (TypeMirror mirror : typeElement.getInterfaces()) {
      if (tool.isSameType(goal, tool.erasure(mirror))) {
        return new Extension(typeElement, asDeclared(mirror));
      }
    }
    return null;
  }

  Optional<TypeMirror> resolveTypevars() {
    if (extensions.isEmpty()) {
      return Optional.empty();
    }
    TypeMirror extensionClass = extensions.get(0).extensionClass();
    for (int i = 1; i < extensions.size(); i++) {
      Extension extension = extensions.get(i);
      extensionClass = resolveStep(extensionClass, extension);
    }
    return Optional.of(extensionClass);
  }

  private TypeMirror resolveStep(TypeMirror x, Extension extension) {
    List<? extends TypeMirror> typeArguments = asDeclared(x).getTypeArguments();
    List<? extends TypeParameterElement> typeParameters = extension.baseClass().getTypeParameters();
    Map<String, TypeMirror> resolution = new HashMap<>();
    for (int i = 0; i < typeParameters.size(); i++) {
      resolution.put(typeParameters.get(i).toString(), typeArguments.get(i));
    }
    return tool.substitute(extension.extensionClass(), resolution)
        .orElse(extension.extensionClass());
  }

  @Override
  public String toString() {
    return extensions.toString();
  }
}
