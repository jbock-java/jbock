package net.jbock.coerce.hint;

import net.jbock.compiler.TypeTool;

import javax.lang.model.element.TypeElement;
import java.util.Set;

abstract class PreciseTypeHint extends Hint {

  @Override
  final String message(TypeElement type, boolean repeatable) {
    TypeTool tool = TypeTool.get();
    for (Class<?> clazz : types()) {
      if (tool.isSameErasure(type.asType(), clazz)) {
        return message(type);
      }
    }
    return null;
  }

  abstract String message(TypeElement typeName);

  abstract Set<Class<?>> types();

}
