package net.jbock.processor;

import javax.lang.model.SourceVersion;
import java.util.List;

/**
 * This is the jbock annotation processor.
 * It extends {@link javax.annotation.processing.AbstractProcessor AbstractProcessor}.
 */
public final class JbockProcessor extends com.google.auto.common.BasicAnnotationProcessor {


    @Override
    protected List<com.google.auto.common.BasicAnnotationProcessor.Step> steps() {
        ProcessorModule module = new ProcessorModule(processingEnv);
        ProcessorComponent component = ProcessorComponent.create(module);
        return List.of(
                component.commandStep(),
                component.methodStep());
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }
}
