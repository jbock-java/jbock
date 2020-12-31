package net.jbock.coerce;

import net.jbock.compiler.TypeTool;

import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static net.jbock.compiler.TypeTool.AS_ARRAY;
import static net.jbock.compiler.TypeTool.AS_TYPEVAR;

public class Unifier {

  private final Types types;

  private final Map<String, TypeMirror> acc = new LinkedHashMap<>();

  public Unifier(Types types) {
    this.types = types;
  }

  /**
   * Resolve typevars in y, to make its type equal to x.
   * If a typevar in x is encountered, that means that we have an
   * extra degree of freedom and we can later replace it with its upper bound,
   * provided that the bound is compatible with y.
   */
  public String unify(TypeMirror x, TypeMirror y) {
    if (y.getKind() == TypeKind.TYPEVAR) {
      if (!types.isAssignable(x, y.accept(AS_TYPEVAR, null).getUpperBound())) {
        return "Unification failed: can't assign " + x + " to " + y;
      }
      acc.put(y.toString(), x);
      return null; // success
    }
    if (x.getKind() == TypeKind.TYPEVAR) {
      if (!types.isAssignable(y, x.accept(AS_TYPEVAR, null).getUpperBound())) {
        return "Unification failed: can't assign " + y + " to " + x;
      }
      return null; // no constraint for y
    }
    if (x.getKind() == TypeKind.WILDCARD || y.getKind() == TypeKind.WILDCARD) {
      return "Unification failed: wildcard is not allowed here";
    }
    if (x.getKind() != y.getKind()) {
      return "Unification failed: " + y + " and " + x + " cannot be unified";
    }
    if (x.getKind() == TypeKind.ARRAY) {
      TypeMirror xc = x.accept(AS_ARRAY, null).getComponentType();
      TypeMirror yc = y.accept(AS_ARRAY, null).getComponentType();
      return unify(xc, yc);
    }
    if (x.getKind() != TypeKind.DECLARED) {
      if (!types.isAssignable(y, x)) {
        return "Unification failed: can't assign " + y + " to " + x;
      }
      return null;
    }
    DeclaredType xx = x.accept(TypeTool.AS_DECLARED, null);
    DeclaredType yy = y.accept(TypeTool.AS_DECLARED, null);
    List<? extends TypeMirror> xargs = xx.getTypeArguments();
    if (!types.isSameType(types.erasure(x), types.erasure(y))) {
      return "Unification failed: " + y + " and " + x + " have different erasure";
    }
    List<? extends TypeMirror> yargs = yy.getTypeArguments();
    if (xargs.size() != yargs.size()) {
      return "Unification failed: " + y + " and " + x + " have different numbers of typeargs";
    }
    for (int i = 0; i < yargs.size(); i++) {
      String failure = unify(xargs.get(i), yargs.get(i));
      if (failure != null) {
        return failure;
      }
    }
    return null;
  }

  public Map<String, TypeMirror> getResult() {
    return acc;
  }
}
