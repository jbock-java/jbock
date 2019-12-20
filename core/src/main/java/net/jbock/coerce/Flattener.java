package net.jbock.coerce;

import net.jbock.coerce.either.Either;
import net.jbock.compiler.TypeTool;
import net.jbock.compiler.TypevarMapping;

import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.TypeMirror;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

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
   * @param leftSolution a mapping
   * @param rightSolution a mapping
   * @return type parameters in the correct order for {@code targetElement}
   */
  Either<String, List<TypeMirror>> getTypeParameters(TypevarMapping leftSolution, TypevarMapping rightSolution) {
    return leftSolution.merge(rightSolution).flatMap(Function.identity(),
        this::getTypeParameters);
  }

  public Either<String, List<TypeMirror>> getTypeParameters(TypevarMapping solution) {
    List<TypeMirror> result = new ArrayList<>();
    List<? extends TypeParameterElement> parameters = targetElement.getTypeParameters();
    for (TypeParameterElement p : parameters) {
      List<? extends TypeMirror> bounds = p.getBounds();
      TypeMirror m = solution.get(p.toString());
      if (m == null) {
        return left("incompatible type");
      }
      if (tool().isOutOfBounds(m, bounds)) {
        return left("Invalid bounds for " + p.toString());
      }
      result.add(m);
    }
    return right(result);
  }

  private TypeTool tool() {
    return basicInfo.tool();
  }
}
