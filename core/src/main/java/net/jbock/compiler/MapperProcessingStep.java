package net.jbock.compiler;

import com.google.auto.common.BasicAnnotationProcessor;
import com.google.common.collect.ImmutableSetMultimap;
import net.jbock.Mapper;

import javax.lang.model.element.Element;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class MapperProcessingStep implements BasicAnnotationProcessor.Step {

  @Override
  public Set<String> annotations() {
    return Stream.of(Mapper.class)
        .map(Class::getCanonicalName)
        .collect(Collectors.toSet());
  }

  @Override
  public Set<? extends Element> process(ImmutableSetMultimap<String, Element> elementsByAnnotation) {
    // TODO check if each annotated element is referenced by some mappedBy attribute
    return Collections.emptySet();
  }
}
