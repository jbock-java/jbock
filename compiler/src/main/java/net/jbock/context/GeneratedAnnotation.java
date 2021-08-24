package net.jbock.context;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.CodeBlock;
import net.jbock.processor.JbockProcessor;

import javax.annotation.processing.Generated;
import javax.inject.Inject;

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
