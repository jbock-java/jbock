package net.jbock.processor;

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

    @Component.Factory
    interface Factory {

        ProcessorComponent create(ProcessorModule module);
    }
}
