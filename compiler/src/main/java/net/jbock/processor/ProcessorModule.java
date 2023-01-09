package net.jbock.processor;

import dagger.Module;
import dagger.Provides;
import dagger.Reusable;
import net.jbock.common.SafeElements;
import net.jbock.common.SafeTypes;
import net.jbock.common.TypeTool;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;

@Module
interface ProcessorModule {

    @Provides
    static Messager messager(ProcessingEnvironment processingEnvironment) {
        return processingEnvironment.getMessager();
    }

    @Provides
    static Filer filer(ProcessingEnvironment processingEnvironment) {
        return processingEnvironment.getFiler();
    }

    @Provides
    @Reusable
    static SafeElements elements(ProcessingEnvironment processingEnvironment) {
        return new SafeElements(processingEnvironment.getElementUtils());
    }

    @Provides
    @Reusable
    static SafeTypes types(ProcessingEnvironment processingEnvironment) {
        return new SafeTypes(processingEnvironment.getTypeUtils());
    }

    @Provides
    @Reusable
    static TypeTool tool(SafeElements elements, SafeTypes types) {
        return new TypeTool(elements, types);
    }
}
