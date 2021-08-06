package net.jbock.processor;

import dagger.Module;
import dagger.Provides;
import net.jbock.common.SafeElements;
import net.jbock.common.SafeTypes;
import net.jbock.common.TypeTool;
import net.jbock.common.Util;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;

/**
 * @see ProcessorScope
 */
@Module
public class ProcessorModule {

    private final ProcessingEnvironment processingEnvironment;

    ProcessorModule(ProcessingEnvironment processingEnvironment) {
        this.processingEnvironment = processingEnvironment;
    }

    @ProcessorScope
    @Provides
    Messager messager() {
        return processingEnvironment.getMessager();
    }

    @ProcessorScope
    @Provides
    Filer filer() {
        return processingEnvironment.getFiler();
    }

    @ProcessorScope
    @Provides
    SafeElements elements() {
        return new SafeElements(processingEnvironment.getElementUtils());
    }

    @ProcessorScope
    @Provides
    SafeTypes types() {
        return new SafeTypes(processingEnvironment.getTypeUtils());
    }

    @ProcessorScope
    @Provides
    TypeTool tool(SafeElements elements, SafeTypes types) {
        return new TypeTool(elements, types);
    }

    @ProcessorScope
    @Provides
    Util util(SafeTypes types, TypeTool tool) {
        return new Util(types, tool);
    }
}
