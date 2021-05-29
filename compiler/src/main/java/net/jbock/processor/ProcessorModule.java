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

@Module
public interface ProcessorModule {

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
  static Types types(ProcessingEnvironment processingEnvironment) {
    return processingEnvironment.getTypeUtils();
  }

  @ProcessorScope
  @Provides
  static TypeTool tool(SafeElements elements,ProcessingEnvironment processingEnv) {
    return new TypeTool(elements, processingEnv.getTypeUtils());
  }

  @ProcessorScope
  @Provides
  static Util util(Types types, TypeTool tool) {
    return new Util(types, tool);
  }
}
