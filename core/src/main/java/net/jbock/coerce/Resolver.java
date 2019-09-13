package net.jbock.coerce;

import net.jbock.compiler.TypeTool;

import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.DeclaredType;
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

  /**
   * Check if {@code start} is a {@code goal}.
   *
   * @param goal a type
   * @param start a type
   * @param tool a tool
   *
   * @return the {@code goal} type, with typevars resolved
   * as far as it can be inferred from {@code start},
   * or {@link Optional#empty() empty} if {@code start}
   * is not a {@code goal}.
   */
  static Optional<TypeMirror> typecheck(
      Class<?> goal,
      TypeElement start,
      TypeTool tool) {
    List<TypeElement> family = getTypeTree(start.asType(), tool);
    Extension extension;
    TypeMirror nextGoal = tool.erasure(goal);
    List<Extension> extensions = new ArrayList<>();
    while ((extension = findExtension(family, nextGoal, tool)) != null) {
      extensions.add(extension);
      nextGoal = tool.erasure(extension.baseClass.asType());
    }
    return new Resolver(extensions, tool).resolveTypevars();
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

  private Optional<TypeMirror> resolveTypevars() {
    if (extensions.isEmpty()) {
      return Optional.empty();
    }
    TypeMirror extensionClass = extensions.get(0).extensionClass;
    for (int i = 1; i < extensions.size(); i++) {
      Extension extension = extensions.get(i);
      TypeMirror stepResult = resolveStep(extensionClass, extension);
      if (extensionClass == null) {
        return Optional.empty();
      }
      extensionClass = stepResult;
    }
    return Optional.of(extensionClass);
  }

  private TypeMirror resolveStep(TypeMirror x, Extension extension) {
    List<? extends TypeMirror> typeArguments = asDeclared(x).getTypeArguments();
    List<? extends TypeParameterElement> typeParameters = extension.baseClass.getTypeParameters();
    Map<String, TypeMirror> solution = new HashMap<>();
    for (int i = 0; i < typeParameters.size(); i++) {
      solution.put(typeParameters.get(i).toString(), typeArguments.get(i));
    }
    DeclaredType input = extension.extensionClass;
    return tool.substitute(input, solution);
  }

  @Override
  public String toString() {
    return extensions.toString();
  }

  static class Extension {

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
}
