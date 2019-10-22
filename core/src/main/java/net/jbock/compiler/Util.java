package net.jbock.compiler;

import com.squareup.javapoet.CodeBlock;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class Util {

  public static boolean hasDefaultConstructor(TypeElement classToCheck) {
    List<ExecutableElement> constructors = ElementFilter.constructorsIn(classToCheck.getEnclosedElements());
    if (constructors.isEmpty()) {
      return true;
    }
    for (ExecutableElement constructor : constructors) {
      if (!constructor.getParameters().isEmpty()) {
        continue;
      }
      if (constructor.getModifiers().contains(Modifier.PRIVATE)) {
        return false;
      }
      return constructor.getThrownTypes().isEmpty();
    }
    return false;
  }

  public static CodeBlock getTypeParameterList(List<TypeMirror> params) {
    if (params.isEmpty()) {
      return CodeBlock.builder().build();
    }
    return CodeBlock.of(Stream.generate(() -> "$T")
        .limit(params.size())
        .collect(Collectors.joining(", ", "<", ">")), params.toArray());
  }
}
