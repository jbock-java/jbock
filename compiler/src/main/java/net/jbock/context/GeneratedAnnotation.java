package net.jbock.context;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.CodeBlock;
import net.jbock.processor.JbockProcessor;

import javax.annotation.processing.Generated;
import javax.inject.Inject;

@ContextScope
public class GeneratedAnnotation extends Cached<AnnotationSpec> {

  private static final String PROJECT_URL = "https://github.com/jbock-java";

  @Inject
  GeneratedAnnotation() {
  }

  @Override
  AnnotationSpec define() {
    return AnnotationSpec.builder(Generated.class)
        .addMember("value", CodeBlock.of("$S", JbockProcessor.class.getCanonicalName()))
        .addMember("comments", CodeBlock.of("$S", PROJECT_URL))
        .build();
  }
}
