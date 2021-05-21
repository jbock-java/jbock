package net.jbock.compiler;

import com.google.auto.common.BasicAnnotationProcessor;
import com.google.common.collect.ImmutableSetMultimap;
import net.jbock.Converter;

import javax.inject.Inject;
import javax.lang.model.element.Element;
import java.util.Set;

/**
 * Ensure that changes in the converter class trigger the processing.
 */
public class ConverterProcessingStep implements BasicAnnotationProcessor.Step {

  @Inject
  ConverterProcessingStep() {
  }

  @Override
  public Set<String> annotations() {
    return Set.of(Converter.class.getCanonicalName());
  }

  @Override
  public Set<? extends Element> process(ImmutableSetMultimap<String, Element> elementsByAnnotation) {
    return Set.of();
  }
}
