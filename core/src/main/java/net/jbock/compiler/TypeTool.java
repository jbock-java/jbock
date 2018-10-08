package net.jbock.compiler;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static net.jbock.compiler.Util.AS_DECLARED;

public class TypeTool {

  private final Types types;

  private final Elements elements;

  private static TypeTool instance;

  private TypeTool(Types types, Elements elements) {
    this.types = types;
    this.elements = elements;
  }

  public static TypeTool get() {
    return instance;
  }

  static void setInstance(Types types, Elements elements) {
    instance = new TypeTool(types, elements);
  }

  static void unset() {
    instance = null;
  }

  private void unify(TypeMirror x, TypeMirror ym, Map<String, TypeMirror> acc, boolean[] failure) {
    if (failure[0]) {
      return;
    }
    if (ym.getKind() == TypeKind.TYPEVAR) {
      acc.put(ym.toString(), x);
      return;
    }
    if (!isSameErasure(x, ym)) {
      failure[0] = true;
      return;
    }
    DeclaredType y = ym.accept(AS_DECLARED, null);
    List<? extends TypeMirror> xargs = x.accept(Util.GET_TYPEARGS, Collections.emptyList());
    List<? extends TypeMirror> yargs = y.getTypeArguments();
    if (xargs.size() != yargs.size()) {
      failure[0] = true;
      return;
    }
    for (int i = 0; i < yargs.size(); i++) {
      TypeMirror yarg = yargs.get(i);
      TypeMirror xarg = xargs.get(i);
      unify(xarg.accept(AS_DECLARED, null), yarg, acc, failure);
    }
  }

  public Optional<Map<String, TypeMirror>> unify(TypeMirror concreteType, TypeMirror ym) {
    Map<String, TypeMirror> acc = new HashMap<>();
    boolean[] failure = new boolean[1];
    unify(concreteType, ym, acc, failure);
    if (failure[0]) {
      return Optional.empty();
    }
    return Optional.of(acc);
  }

  public TypeMirror substitute(TypeMirror input, Map<String, TypeMirror> solution) {
    if (input.getKind() == TypeKind.TYPEVAR) {
      TypeMirror result = solution.get(input.toString());
      if (result == null) {
        throw new IllegalArgumentException();
      }
      return result;
    }
    DeclaredType result = input.accept(AS_DECLARED, null);
    TypeElement erasure = result.asElement().accept(Util.AS_TYPE_ELEMENT, null);
    List<TypeMirror> args = new ArrayList<>();
    for (TypeMirror typeArgument : result.getTypeArguments()) {
      args.add(substitute(typeArgument, solution));
    }
    return types.getDeclaredType(erasure, args.toArray(new TypeMirror[0]));
  }

  boolean isAssignable(DeclaredType x, TypeMirror ym) {
    Optional<Map<String, TypeMirror>> solution = unify(x, ym);
    if (!solution.isPresent()) {
      return false;
    }
    return types.isSameType(x, substitute(ym, solution.get()));
  }

  public boolean equals(TypeMirror mirror, Class<?> test) {
    return types.isSameType(mirror, elements.getTypeElement(test.getCanonicalName()).asType());
  }

  public boolean equals(TypeMirror mirror, TypeMirror test) {
    return types.isSameType(mirror, test);
  }

  public TypeMirror box(TypeMirror mirror) {
    return types.boxedClass(((PrimitiveType) mirror)).asType();
  }

  public PrimitiveType primitive(TypeKind kind) {
    return types.getPrimitiveType(kind);
  }

  private boolean isSameErasure(TypeMirror x, TypeMirror y) {
    return types.isSameType(types.erasure(x), types.erasure(y));
  }

  public TypeMirror erasure(TypeMirror typeMirror) {
    return types.erasure(typeMirror);
  }

  public DeclaredType declared(String type) {
    return types.getDeclaredType(elements.getTypeElement(type));
  }

  public DeclaredType declared(Class<?> type) {
    return declared(type.getCanonicalName());
  }

  DeclaredType declared(String type, String typevar0) {
    return types.getDeclaredType(elements.getTypeElement(type), elements.getTypeElement(typevar0).asType());
  }

  DeclaredType declared(String type, String typevar0, String typevar1) {
    return types.getDeclaredType(elements.getTypeElement(type),
        elements.getTypeElement(typevar0).asType(),
        elements.getTypeElement(typevar1).asType());
  }

  DeclaredType declared(String type, String typevar0, DeclaredType typevar1) {
    return types.getDeclaredType(elements.getTypeElement(type),
        elements.getTypeElement(typevar0).asType(),
        typevar1);
  }
}
