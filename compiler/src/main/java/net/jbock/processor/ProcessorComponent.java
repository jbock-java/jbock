package net.jbock.processor;

import net.jbock.common.SafeElements;
import net.jbock.common.SafeTypes;
import net.jbock.common.TypeTool;
import net.jbock.common.Util;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;

final class ProcessorComponent {

    private final Util util;
    private final TypeTool tool;
    private final Messager messager;
    private final SourceFileGenerator sourceFileGenerator;

    ProcessorComponent(ProcessingEnvironment processingEnvironment) {
        SafeTypes types = new SafeTypes(processingEnvironment.getTypeUtils());
        SafeElements elements = new SafeElements(processingEnvironment.getElementUtils());
        Filer filer = processingEnvironment.getFiler();
        this.tool = new TypeTool(elements, types);
        this.util = new Util(types, tool);
        this.messager = processingEnvironment.getMessager();
        this.sourceFileGenerator = new SourceFileGenerator(filer, messager);
    }

    MethodStep methodStep() {
        return new MethodStep(messager, util);
    }

    CommandStep commandStep() {
        return new CommandStep(messager, util, sourceFileGenerator, tool);
    }
}
