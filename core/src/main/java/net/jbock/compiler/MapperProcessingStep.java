package net.jbock.compiler;

import com.google.auto.common.BasicAnnotationProcessor;
import com.google.common.collect.ImmutableSetMultimap;
import net.jbock.Converter;

import javax.inject.Inject;
import javax.lang.model.element.Element;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MapperProcessingStep implements BasicAnnotationProcessor.Step {

  @Inject
  MapperProcessingStep() {
  }

  @Override
  public Set<String> annotations() {
    return Stream.of(Converter.class)
        .map(Class::getCanonicalName)
        .collect(Collectors.toSet());
  }

  @Override
  public Set<? extends Element> process(ImmutableSetMultimap<String, Element> elementsByAnnotation) {
    // TODO check if each annotated element is referenced by some mappedBy attribute
    return Collections.emptySet();
  }
}
