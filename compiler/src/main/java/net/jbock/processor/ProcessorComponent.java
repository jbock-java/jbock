package net.jbock.processor;

import dagger.BindsInstance;
import dagger.Component;

import javax.annotation.processing.ProcessingEnvironment;

/**
 * @see ProcessorScope
 */
@Component(modules = ProcessorModule.class)
@ProcessorScope
interface ProcessorComponent {

    ParameterMethodProcessingStep parameterMethodProcessingStep();

    ConverterProcessingStep converterProcessingStep();

    CommandProcessingStep commandProcessingStep();

    @Component.Builder
    interface Builder {

        @BindsInstance
        Builder processingEnv(ProcessingEnvironment processingEnvironment);

        ProcessorComponent build();
    }
}
