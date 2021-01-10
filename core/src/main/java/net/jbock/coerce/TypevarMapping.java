package net.jbock.coerce;

import net.jbock.coerce.either.Either;
import net.jbock.compiler.TypeTool;
import net.jbock.compiler.ValidationException;

import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static net.jbock.coerce.either.Either.left;
import static net.jbock.coerce.either.Either.right;
import static net.jbock.compiler.TypeTool.AS_DECLARED;
import static net.jbock.compiler.TypeTool.AS_INTERSECTION;
import static net.jbock.compiler.TypeTool.AS_TYPEVAR;

public class TypevarMapping {

  private final Map<String, TypeMirror> map;

  private final TypeTool tool;

  private final Types types;

  private final Function<String, ValidationException> errorHandler;

  public TypevarMapping(Map<String, TypeMirror> map, TypeTool tool, Function<String, ValidationException> errorHandler) {
    this.map = map;
    this.tool = tool;
    this.types = tool.types();
    this.errorHandler = errorHandler;
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
  public TypeMirror substitute(TypeMirror input) {
    if (input.getKind() == TypeKind.TYPEVAR) {
      return map.getOrDefault(input.toString(), input);
    }
    if (input.getKind() == TypeKind.ARRAY) {
      return tool.getArrayType(substitute(input.accept(TypeTool.AS_ARRAY, null).getComponentType()));
    }
    if (input.getKind() != TypeKind.DECLARED) {
      return input;
    }
    return substitute(input.accept(AS_DECLARED, null));
  }

  private DeclaredType substitute(DeclaredType declaredType) {
    List<? extends TypeMirror> typeArguments = declaredType.getTypeArguments();
    if (typeArguments.isEmpty()) {
      return declaredType;
    }
    TypeMirror[] result = new TypeMirror[typeArguments.size()];
    for (int i = 0; i < typeArguments.size(); i++) {
      TypeMirror arg = typeArguments.get(i);
      switch (arg.getKind()) {
        case TYPEVAR:
          result[i] = map.getOrDefault(arg.toString(), arg);
          break;
        case DECLARED:
          result[i] = substitute(TypeTool.asDeclared(arg)); // recursion
          break;
        case WILDCARD:
          result[i] = arg;
        case ARRAY:
          result[i] = tool.getArrayType(substitute(arg.accept(TypeTool.AS_ARRAY, null).getComponentType()));
          break;
        default:
          throw errorHandler.apply("substitution failed: unknown typearg " + arg);
      }
    }
    return types.getDeclaredType(tool.asTypeElement(declaredType), result);
  }

  public Either<String, TypevarMapping> merge(TypevarMapping solution) {
    Map<String, TypeMirror> result = new LinkedHashMap<>(map);
    for (String key : solution.map.keySet()) {
      TypeMirror solutionType = solution.get(key);
      if (map.containsKey(key)) {
        TypeMirror mapType = map.get(key);
        if (!tool.types().isSameType(mapType, solutionType)) {
          return left(String.format("Conflicting solutions for " + key +
              ": %s vs %s", mapType, solutionType));
        }
      }
      result.put(key, solutionType);
    }
    return right(new TypevarMapping(result, tool, errorHandler));
  }

  public Either<String, List<TypeMirror>> getTypeParameters(TypeElement targetElement) {
    List<? extends TypeParameterElement> parameters = targetElement.getTypeParameters();
    List<TypeMirror> result = new ArrayList<>(parameters.size());
    for (TypeParameterElement p : parameters) {
      TypeMirror m = map.getOrDefault(p.toString(), p.asType());
      if (m.getKind() == TypeKind.TYPEVAR) {
        m = m.accept(AS_TYPEVAR, null).getUpperBound();
      }
      if (m.getKind() == TypeKind.INTERSECTION) {
        m = m.accept(AS_INTERSECTION, null).getBounds().get(0);
      }
      result.add(m);
    }
    return right(result);
  }
}
