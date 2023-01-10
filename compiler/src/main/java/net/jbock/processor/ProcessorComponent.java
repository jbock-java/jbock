package net.jbock.processor;

import net.jbock.common.SafeElements;
import net.jbock.common.SafeTypes;
import net.jbock.common.TypeTool;
import net.jbock.common.Util;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import java.util.function.Supplier;

import static net.jbock.common.Suppliers.memoize;

final class ProcessorComponent {

    private final Supplier<Util> utilProvider;
    private final Supplier<SafeTypes> typesProvider;
    private final Supplier<SafeElements> elementsProvider;
    private final Supplier<TypeTool> toolProvider;
    private final Supplier<Filer> filerProvider;
    private final Supplier<Messager> messagerProvider;
    private final Supplier<SourceFileGenerator> sourceFileGeneratorProvider;

    ProcessorComponent(ProcessingEnvironment processingEnvironment) {
        this.typesProvider = memoize(() -> new SafeTypes(processingEnvironment.getTypeUtils()));
        this.elementsProvider = memoize(() -> new SafeElements(processingEnvironment.getElementUtils()));
        this.toolProvider = memoize(() -> new TypeTool(elementsProvider.get(), typesProvider.get()));
        this.utilProvider = memoize(() -> new Util(typesProvider.get(), toolProvider.get()));
        this.filerProvider = memoize(processingEnvironment::getFiler);
        this.messagerProvider = memoize(processingEnvironment::getMessager);
        this.sourceFileGeneratorProvider = memoize(() -> new SourceFileGenerator(filerProvider.get(), messagerProvider.get()));
    }

    MethodStep methodStep() {
        return new MethodStep(messagerProvider.get(), utilProvider.get());
    }

    CommandStep commandStep() {
        return new CommandStep(messagerProvider.get(), utilProvider.get(), sourceFileGeneratorProvider.get(), toolProvider.get());
    }
}
