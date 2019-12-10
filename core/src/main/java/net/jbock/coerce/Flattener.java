package net.jbock.coerce;

import net.jbock.coerce.either.Either;
import net.jbock.compiler.TypeTool;

import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.TypeMirror;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
    return result.flatMap(this::getTypeParameters);
  }

  public Either<String, List<TypeMirror>> getTypeParameters(Map<String, TypeMirror> solution) {
    List<? extends TypeParameterElement> ps = targetElement.getTypeParameters();
    List<TypeMirror> outcome = ps.stream()
        .map(TypeParameterElement::toString)
        .map(solution::get)
        .collect(Collectors.toList());
    if (!passesBoundsCheck(outcome)) {
      return Either.left("invalid bounds");
    }
    return Either.right(outcome);
  }

  private boolean passesBoundsCheck(List<TypeMirror> outcome) {
    List<? extends TypeParameterElement> ps = targetElement.getTypeParameters();
    for (int i = 0; i < ps.size(); i++) {
      TypeParameterElement p = ps.get(i);
      List<? extends TypeMirror> bounds = p.getBounds();
      TypeMirror m = outcome.get(i);
      if (tool().isOutOfBounds(m, bounds)) {
        return false;
      }
    }
    return true;
  }

  private Either<String, Map<String, TypeMirror>> mergeResult(List<Map<String, TypeMirror>> results) {
    Map<String, TypeMirror> out = new LinkedHashMap<>();
    for (Map<String, TypeMirror> result : results) {
      for (Map.Entry<String, TypeMirror> entry : result.entrySet()) {
        TypeMirror current = out.get(entry.getKey());
        if (current != null) {
          if (!tool().isSameType(current, entry.getValue())) {
            return Either.left("invalid bounds");
          }
        }
        out.put(entry.getKey(), entry.getValue());
      }
    }
    return Either.right(out);
  }

  private TypeTool tool() {
    return basicInfo.tool();
  }
}
