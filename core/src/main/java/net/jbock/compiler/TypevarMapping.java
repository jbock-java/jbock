package net.jbock.compiler;

import net.jbock.coerce.FlattenerResult;
import net.jbock.coerce.either.Either;
import net.jbock.coerce.either.Left;
import net.jbock.coerce.either.Right;

import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static net.jbock.coerce.either.Either.left;
import static net.jbock.coerce.either.Either.right;
import static net.jbock.compiler.TypeTool.AS_DECLARED;

public class TypevarMapping {

  private final Map<String, TypeMirror> map;

  private final TypeTool tool;

  private final Function<String, ValidationException> errorHandler;

  public TypevarMapping(Map<String, TypeMirror> map, TypeTool tool, Function<String, ValidationException> errorHandler) {
    this.map = map;
    this.tool = tool;
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
      return input; // TODO generic array?
    }
    return substitute(input.accept(AS_DECLARED, null));
  }

  private DeclaredType substitute(DeclaredType declaredType) {
    List<? extends TypeMirror> typeArguments = declaredType.getTypeArguments();
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
        case ARRAY:
          result[i] = arg;
          break;
        default:
          throw errorHandler.apply("substitution failed");
      }
    }
    return tool.getDeclaredType(tool.asTypeElement(declaredType), result);
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
    return right(new TypevarMapping(result, tool, errorHandler));
  }

  public Either<String, FlattenerResult> getTypeParameters(TypeElement targetElement) {
    List<TypeMirror> result = new ArrayList<>();
    Map<String, TypeMirror> mapping = new LinkedHashMap<>(map);
    List<? extends TypeParameterElement> parameters = targetElement.getTypeParameters();
    for (TypeParameterElement p : parameters) {
      TypeMirror m = map.get(p.toString());
      if (m == null || m.getKind() == TypeKind.TYPEVAR) {
        Either<String, TypeMirror> inferred = tool.getBound(p);
        if (inferred instanceof Left) {
          return left(((Left<String, TypeMirror>) inferred).value());
        }
        m = ((Right<String, TypeMirror>) inferred).value();
      }
      if (tool.isOutOfBounds(m, p.getBounds())) {
        return left("Invalid bounds: Can't resolve " + p.toString() + " to " + m);
      }
      mapping.put(p.toString(), m);
      result.add(m);
    }
    return right(new FlattenerResult(result, new TypevarMapping(mapping, tool, errorHandler)));
  }
}
