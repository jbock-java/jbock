package net.jbock.compiler;

import com.google.auto.common.BasicAnnotationProcessor;
import com.google.common.collect.ImmutableSetMultimap;
import net.jbock.Command;
import net.jbock.Option;
import net.jbock.Param;

import javax.annotation.processing.Messager;
import javax.inject.Inject;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.util.ElementFilter;
import javax.tools.Diagnostic;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class ParameterMethodProcessingStep implements BasicAnnotationProcessor.Step {

  private final Messager messager;

  @Inject
  ParameterMethodProcessingStep(Messager messager) {
    this.messager = messager;
  }

  @Override
  public Set<String> annotations() {
    return Stream.of(Option.class, Param.class)
        .map(Class::getCanonicalName)
        .collect(Collectors.toSet());
  }

  @Override
  public Set<? extends Element> process(ImmutableSetMultimap<String, Element> elementsByAnnotation) {
    for (ExecutableElement method : ElementFilter.methodsIn(elementsByAnnotation.values())) {
      checkEnclosingElementIsAnnotated(method);
    }
    return Collections.emptySet();
  }

  private void checkEnclosingElementIsAnnotated(ExecutableElement method) {
    Element enclosingElement = method.getEnclosingElement();
    if (enclosingElement.getAnnotation(Command.class) == null) {
      messager.printMessage(Diagnostic.Kind.ERROR,
          "put @" + Command.class.getSimpleName() + " annotation on the enclosing class", method);
    }
    if (!method.getModifiers().contains(Modifier.ABSTRACT)) {
      messager.printMessage(Diagnostic.Kind.ERROR, "abstract method expected");
    }
  }
}
