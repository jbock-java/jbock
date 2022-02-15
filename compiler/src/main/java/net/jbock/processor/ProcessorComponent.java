package net.jbock.processor;

import dagger.BindsInstance;
import dagger.Component;

import javax.annotation.processing.ProcessingEnvironment;

/**
 * @see ProcessorScope
 */
@ProcessorScope
@Component(modules = ProcessorModule.class)
interface ProcessorComponent {

    MethodStep methodStep();

    CommandStep commandStep();

    static ProcessorComponent create(ProcessingEnvironment processingEnvironment) {
        return DaggerProcessorComponent.factory().create(processingEnvironment);
    }

    @Component.Factory
    interface Factory {

        ProcessorComponent create(@BindsInstance ProcessingEnvironment processingEnvironment);
    }
}
