package net.jbock.processor;

import dagger.Module;
import dagger.Provides;
import net.jbock.common.SafeElements;
import net.jbock.common.SafeTypes;
import net.jbock.common.TypeTool;
import net.jbock.common.Util;
import net.jbock.validate.ValidateComponent;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;

/**
 * @see ProcessorScope
 */
@Module(subcomponents = ValidateComponent.class)
interface ProcessorModule {

    @ProcessorScope
    @Provides
    static Messager messager(ProcessingEnvironment processingEnvironment) {
        return processingEnvironment.getMessager();
    }

    @ProcessorScope
    @Provides
    static Filer filer(ProcessingEnvironment processingEnvironment) {
        return processingEnvironment.getFiler();
    }

    @ProcessorScope
    @Provides
    static SafeElements elements(ProcessingEnvironment processingEnvironment) {
        return new SafeElements(processingEnvironment.getElementUtils());
    }

    @ProcessorScope
    @Provides
    static SafeTypes types(ProcessingEnvironment processingEnvironment) {
        return new SafeTypes(processingEnvironment.getTypeUtils());
    }

    @ProcessorScope
    @Provides
    static TypeTool tool(SafeElements elements, SafeTypes types) {
        return new TypeTool(elements, types);
    }

    @ProcessorScope
    @Provides
    static Util util(SafeTypes types, TypeTool tool) {
        return new Util(types, tool);
    }
}
