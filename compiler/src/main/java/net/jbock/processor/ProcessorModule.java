package net.jbock.processor;

import dagger.Module;
import dagger.Provides;
import net.jbock.common.SafeElements;
import net.jbock.common.TypeTool;
import net.jbock.common.Util;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.util.Types;

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
    Types types() {
        return processingEnvironment.getTypeUtils();
    }

    @ProcessorScope
    @Provides
    TypeTool tool(SafeElements elements) {
        return new TypeTool(elements, processingEnvironment.getTypeUtils());
    }

    @ProcessorScope
    @Provides
    Util util(Types types, TypeTool tool) {
        return new Util(types, tool);
    }
}
