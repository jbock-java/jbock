package net.jbock.writing;

import io.jbock.javapoet.AnnotationSpec;
import io.jbock.javapoet.CodeBlock;
import io.jbock.simple.Inject;
import net.jbock.processor.JbockProcessor;

import javax.annotation.processing.Generated;
import java.util.Objects;

final class GeneratedAnnotation {

    @Inject
    GeneratedAnnotation() {
    }

    AnnotationSpec define() {
        return AnnotationSpec.builder(Generated.class)
                .addMember("value", CodeBlock.of("$S", JbockProcessor.class.getCanonicalName()))
                .addMember("comments", CodeBlock.of("$S", getComments()))
                .build();
    }

    private String getComments() {
        String version = Objects.toString(getClass().getPackage().getImplementationVersion(), "");
        return "https://github.com/jbock-java/jbock" + (version.isEmpty() ? "" : " " + version);
    }
}
