package net.jbock.coerce;

import net.jbock.coerce.either.Either;
import net.jbock.compiler.TypeTool;

import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.TypeMirror;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Flattener {

  private final BasicInfo basicInfo;
  private final TypeElement targetElement;

  public Flattener(BasicInfo basicInfo, TypeElement targetElement) {
    this.targetElement = targetElement;
    this.basicInfo = basicInfo;
  }

  /**
   * @param partialSolutions named type parameters
   * @return type parameters in the correct order for {@code targetElement}
   */
  Either<String, List<TypeMirror>> getTypeParameters(List<Map<String, TypeMirror>> partialSolutions) {
    Either<String, Map<String, TypeMirror>> result = mergeResult(partialSolutions);
    return result.flatMap(Function.identity(), this::getTypeParameters);
  }

  public Either<String, List<TypeMirror>> getTypeParameters(Map<String, TypeMirror> solution) {
    List<TypeMirror> outcome = targetElement.getTypeParameters().stream()
        .map(TypeParameterElement::toString)
        .map(solution::get)
        .collect(Collectors.toList());
    String errorMessage = boundsCheck(outcome);
    if (errorMessage != null) {
      return Either.left(errorMessage);
    }
    return Either.right(outcome);
  }

  private String boundsCheck(List<TypeMirror> outcome) {
    List<? extends TypeParameterElement> parameters = targetElement.getTypeParameters();
    for (int i = 0; i < parameters.size(); i++) {
      TypeParameterElement p = parameters.get(i);
      List<? extends TypeMirror> bounds = p.getBounds();
      TypeMirror m = outcome.get(i);
      if (m == null) {
        return "incompatible type";
      }
      if (tool().isOutOfBounds(m, bounds)) {
        return "invalid bounds";
      }
    }
    return null;
  }

  private Either<String, Map<String, TypeMirror>> mergeResult(List<Map<String, TypeMirror>> partialSolutions) {
    Map<String, TypeMirror> result = new LinkedHashMap<>();
    for (Map<String, TypeMirror> solution : partialSolutions) {
      for (Map.Entry<String, TypeMirror> entry : solution.entrySet()) {
        TypeMirror current = result.get(entry.getKey());
        if (current != null && !tool().isSameType(current, entry.getValue())) {
          return Either.left("invalid bounds");
        }
        result.put(entry.getKey(), entry.getValue());
      }
    }
    return Either.right(result);
  }

  private TypeTool tool() {
    return basicInfo.tool();
  }
}
