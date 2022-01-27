package net.jbock.context;

import io.jbock.javapoet.AnnotationSpec;
import io.jbock.javapoet.CodeBlock;
import jakarta.inject.Inject;
import net.jbock.processor.JbockProcessor;

import javax.annotation.processing.Generated;

@ContextScope
public class GeneratedAnnotation {

    @Inject
    GeneratedAnnotation() {
    }

    AnnotationSpec define() {
        return AnnotationSpec.builder(Generated.class)
                .addMember("value", CodeBlock.of("$S", JbockProcessor.class.getCanonicalName()))
                .addMember("comments", CodeBlock.of("$S", "https://github.com/jbock-java"))
                .build();
    }
}
