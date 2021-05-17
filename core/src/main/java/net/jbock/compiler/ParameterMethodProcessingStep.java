package net.jbock.compiler;

import com.google.auto.common.BasicAnnotationProcessor;
import com.google.common.collect.ImmutableSetMultimap;
import net.jbock.Option;
import net.jbock.Parameter;
import net.jbock.Parameters;

import javax.annotation.processing.Messager;
import javax.inject.Inject;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.util.ElementFilter;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static javax.lang.model.element.Modifier.ABSTRACT;
import static javax.tools.Diagnostic.Kind.ERROR;

public class ParameterMethodProcessingStep implements BasicAnnotationProcessor.Step {

  private final Messager messager;

  @Inject
  ParameterMethodProcessingStep(Messager messager) {
    this.messager = messager;
  }

  @Override
  public Set<String> annotations() {
    return Stream.of(Option.class, Parameter.class, Parameters.class)
        .map(Class::getCanonicalName)
        .collect(Collectors.toSet());
  }

  @Override
  public Set<? extends Element> process(ImmutableSetMultimap<String, Element> elementsByAnnotation) {
    for (ExecutableElement method : ElementFilter.methodsIn(elementsByAnnotation.values())) {
      checkEnclosingElement(method);
    }
    return Collections.emptySet();
  }

  private void checkEnclosingElement(ExecutableElement method) {
    Element enclosing = method.getEnclosingElement();
    if (enclosing.getKind().isInterface()) {
      messager.printMessage(ERROR, "use an abstract class, not an interface", enclosing);
    }
    if (!method.getModifiers().contains(ABSTRACT)) {
      messager.printMessage(ERROR, "abstract method expected", method);
    }
  }
}
