package net.jbock.processor;

import dagger.Component;

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
