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

import static net.jbock.coerce.either.Either.left;
import static net.jbock.coerce.either.Either.right;

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
    return mergeSolutions(partialSolutions).flatMap(Function.identity(),
        this::getTypeParameters);
  }

  public Either<String, List<TypeMirror>> getTypeParameters(Map<String, TypeMirror> solution) {
    return boundsCheck(targetElement.getTypeParameters().stream()
        .map(TypeParameterElement::toString)
        .map(solution::get)
        .collect(Collectors.toList()));
  }

  private Either<String, List<TypeMirror>> boundsCheck(List<TypeMirror> solution) {
    List<? extends TypeParameterElement> parameters = targetElement.getTypeParameters();
    for (int i = 0; i < parameters.size(); i++) {
      TypeParameterElement p = parameters.get(i);
      List<? extends TypeMirror> bounds = p.getBounds();
      TypeMirror m = solution.get(i);
      if (m == null) {
        return left("incompatible type");
      }
      if (tool().isOutOfBounds(m, bounds)) {
        return left("invalid bounds");
      }
    }
    return right(solution);
  }

  private Either<String, Map<String, TypeMirror>> mergeSolutions(List<Map<String, TypeMirror>> partialSolutions) {
    Map<String, TypeMirror> result = new LinkedHashMap<>();
    for (Map<String, TypeMirror> solution : partialSolutions) {
      for (Map.Entry<String, TypeMirror> entry : solution.entrySet()) {
        TypeMirror test = result.get(entry.getKey());
        if (test != null && !tool().isSameType(test, entry.getValue())) {
          return left("invalid bounds");
        }
        result.put(entry.getKey(), entry.getValue());
      }
    }
    return right(result);
  }

  private TypeTool tool() {
    return basicInfo.tool();
  }
}
