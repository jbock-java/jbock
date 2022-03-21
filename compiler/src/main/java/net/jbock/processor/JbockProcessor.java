package net.jbock.processor;

import io.jbock.auto.common.BasicAnnotationProcessor;

import javax.lang.model.SourceVersion;
import java.util.List;

/**
 * This is the jbock annotation processor.
 * It extends {@link javax.annotation.processing.AbstractProcessor AbstractProcessor}.
 */
public final class JbockProcessor extends BasicAnnotationProcessor {

    @Override
    protected List<Step> steps() {
        ProcessorComponent component = ProcessorComponent.create(processingEnv);
        return List.of(
                component.commandStep(),
                component.methodStep());
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }
}
