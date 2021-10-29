package net.jbock.processor;

import dagger.Component;

/**
 * @see ProcessorScope
 */
@Component(modules = ProcessorModule.class)
@ProcessorScope
interface ProcessorComponent {

    MethodStep methodStep();

    CommandStep commandStep();

    static ProcessorComponent create(ProcessorModule module) {
        return DaggerProcessorComponent.factory().create(module);
    }

    @Component.Factory
    interface Factory {

        ProcessorComponent create(ProcessorModule module);
    }
}
