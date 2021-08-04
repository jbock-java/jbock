package net.jbock.processor;

import dagger.Component;

/**
 * @see ProcessorScope
 */
@Component(modules = ProcessorModule.class)
@ProcessorScope
interface ProcessorComponent {

    MethodStep methodStep();

    ConverterStep converterStep();

    CommandStep commandStep();

    @Component.Factory
    interface Factory {

        ProcessorComponent create(ProcessorModule module);
    }
}
