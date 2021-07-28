package net.jbock.processor;

import com.google.auto.common.BasicAnnotationProcessor;

import javax.lang.model.SourceVersion;
import java.util.List;

/**
 * This is jbock's annotation processor.
 * It defines "steps", which are like subprocessors,
 * each of which handles a subset of the jbock annotations.
 * Most validation, as well as the source code generation,
 * is handled in the {@link CommandProcessingStep}.
 * The other steps perform some additional validation.
 */
public final class JbockProcessor extends BasicAnnotationProcessor {

    @Override
    protected Iterable<? extends Step> steps() {
        ProcessorComponent component = DaggerProcessorComponent.builder()
                .processingEnv(processingEnv)
                .build();
        return List.of(component.commandProcessingStep(),
                component.converterProcessingStep(),
                component.parameterMethodProcessingStep());
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }
}
