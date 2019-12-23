package net.jbock.compiler;

import net.jbock.coerce.either.Either;
import net.jbock.coerce.either.Left;
import net.jbock.coerce.either.Right;
import net.jbock.coerce.reference.TypecheckFailure;

import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import static net.jbock.coerce.either.Either.left;
import static net.jbock.coerce.either.Either.right;
import static net.jbock.compiler.TypeTool.AS_DECLARED;

public class TypevarMapping {

  private final Map<String, TypeMirror> map;

  private final TypeTool tool;

  public TypevarMapping(Map<String, TypeMirror> map, TypeTool tool) {
    this.map = map;
    this.tool = tool;
  }

  public static TypevarMapping empty(TypeTool tool) {
    return new TypevarMapping(Collections.emptyMap(), tool);
  }

  public Set<Map.Entry<String, TypeMirror>> entries() {
    return map.entrySet();
  }

  public TypeMirror get(String key) {
    return map.get(key);
  }

  /**
   * @param input a type
   * @return the input type, with all typevars resolved.
   * Can be null.
   * Wildcards remain unchanged.
   */
  public Either<TypecheckFailure, TypeMirror> substitute(TypeMirror input) {
    if (input.getKind() == TypeKind.TYPEVAR) {
      return right(map.getOrDefault(input.toString(), input));
    }
    if (input.getKind() == TypeKind.ARRAY) {
      return right(input);
    }
    return substitute(input.accept(AS_DECLARED, null))
        .map(Function.identity(), type -> type);
  }

  public Either<TypecheckFailure, DeclaredType> substitute(DeclaredType declaredType) {
    DeclaredType result = subst(declaredType);
    if (result == null) {
      return left(TypecheckFailure.fatal("substitution failed"));
    }
    return right(result);
  }

  private DeclaredType subst(DeclaredType input) {
    List<? extends TypeMirror> typeArguments = input.getTypeArguments();
    TypeMirror[] result = new TypeMirror[typeArguments.size()];
    for (int i = 0; i < typeArguments.size(); i++) {
      result[i] = switchType(typeArguments.get(i));
      if (result[i] == null) {
        return null;  // error
      }
    }
    return tool.getDeclaredType(tool.asTypeElement(input), result);
  }

  private TypeMirror switchType(TypeMirror input) {
    switch (input.getKind()) {
      case WILDCARD:
        return input; // these can stay
      case TYPEVAR:
        return map.getOrDefault(input.toString(), input);
      case DECLARED:
        return subst(TypeTool.asDeclared(input));
      case ARRAY:
        return input;
      default:
        return null;
    }
  }

  public Either<String, TypevarMapping> merge(TypevarMapping solution) {
    Map<String, TypeMirror> result = new LinkedHashMap<>(map);
    for (String key : solution.map.keySet()) {
      TypeMirror thisType = map.get(key);
      TypeMirror thatType = solution.get(key);
      if (thisType != null) {
        Either<Function<String, String>, TypeMirror> specialization = tool.getSpecialization(thisType, thatType);
        if (specialization instanceof Left) {
          return left(((Left<Function<String, String>, TypeMirror>) specialization).value().apply(key));
        }
        result.put(key, ((Right<Function<String, String>, TypeMirror>) specialization).value());
      } else {
        result.put(key, thatType);
      }
    }
    return right(new TypevarMapping(result, tool));
  }
}
